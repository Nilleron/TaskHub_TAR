package brendel.projekt.aufgabenverwaltung.controller;

import brendel.projekt.aufgabenverwaltung.models.MyUser;
import brendel.projekt.aufgabenverwaltung.models.Project;
import brendel.projekt.aufgabenverwaltung.models.Status;
import brendel.projekt.aufgabenverwaltung.models.Task;
import brendel.projekt.aufgabenverwaltung.services.EmailService;
import brendel.projekt.aufgabenverwaltung.services.repositories.MyUserRepository;
import brendel.projekt.aufgabenverwaltung.services.repositories.ProjectRepository;
import brendel.projekt.aufgabenverwaltung.services.repositories.TaskRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

@Controller
public class DashboardController {

    @Autowired
    private MyUserRepository myUserRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private EmailService emailService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        MyUser myUser = myUserRepository.findUserByUsername(principal.getName());

        List<Task> tasks = taskRepository.findAllByAssignedToIdOrProjectOwnerIdOrderByStatusDescDueDateAscUpdatedAtDesc(myUser.getId(), myUser.getId());

        List<Project> projects = projectRepository.findAllByOwnerIdOrderByStatusDescDueDateAscUpdatedAtDesc(myUser.getId());
        projects.addAll(tasks.stream()
                .map(Task::getProject)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        projects = projects.stream()
                .distinct()
                .toList();

        model.addAttribute("user", myUser);
        model.addAttribute("tasks", tasks);
        model.addAttribute("projects", projects);

        return "dashboard/index";
    }

    @GetMapping("/dashboard/task")
    public String editTask(Model model, Principal principal, @RequestParam Integer id, RedirectAttributes redirectAttributes) {

        Task editTask = taskRepository.findTaskById(id);
        MyUser myUser = myUserRepository.findUserByUsername(principal.getName());

        if (myUser.getRole().getPriority() >= 600) {
            redirectAttributes.addAttribute("id", id);
            return "redirect:/tasks/edit";
        }

        if (!editTask.getAssignedTo().equals(myUser)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("task", editTask);
        model.addAttribute("actDays", 0.0f);
        model.addAttribute("user", myUser);

        return "dashboard/task";
    }

    @GetMapping("/dashboard/task/start")
    public String startTask(@RequestParam Integer id, RedirectAttributes redirectAttributes) throws MessagingException {

        Task editTask = taskRepository.findTaskById(id);
        if (!editTask.getStatus().equals(Status.open)) {
            redirectAttributes.addFlashAttribute("error", "This task is already in progress or completed");
            redirectAttributes.addAttribute("id", id);
            return "redirect:/dashboard/task";
        }

        Project project = projectRepository.findProjectById(editTask.getProject().getId());
        if (project.getStatus() != Status.in_progress) {
            project.setStatus(Status.in_progress);
            projectRepository.logUpdate(project);
        }

        editTask.setStatus(Status.in_progress);
        taskRepository.logUpdate(editTask);

        redirectAttributes.addFlashAttribute("message", "Task started");
        redirectAttributes.addAttribute("id", id);
        return "redirect:/dashboard/task";
    }

    @PostMapping("/dashboard/task/complete")
    public String finishTask(@ModelAttribute("task") Task task, RedirectAttributes redirectAttributes) throws MessagingException {

        if (!task.getStatus().equals(Status.in_progress)) {
            redirectAttributes.addFlashAttribute("error", "This task is still open or already completed");
            redirectAttributes.addAttribute("id", task.getId());
            return "redirect:/dashboard/task";
        }

        if (task.getActualDays() < 0.0f) {
            redirectAttributes.addFlashAttribute("error", "Days must be greater than zero");
            redirectAttributes.addAttribute("id", task.getId());
            return "redirect:/dashboard/task";
        }

        task.setStatus(Status.completed);
        taskRepository.logUpdate(task);

        List<Task> tasks = taskRepository.findAllByProjectId(task.getProject().getId());
        Project project = projectRepository.findProjectById(task.getProject().getId());
        project.setActualDays(0.0f);
        for (Task t : tasks) {
            project.setActualDays(project.getActualDays() + t.getActualDays());
        }
        projectRepository.logUpdate(project);
        if (project.getOwner().getEmail() != null && !project.getOwner().getEmail().isEmpty()) {
            emailService.sendTaskCompleted(taskRepository.findTaskById(task.getId()));
        }

        redirectAttributes.addFlashAttribute("message", "Task finished");
        redirectAttributes.addAttribute("id", task.getId());
        return "redirect:/dashboard/task";
    }
}
