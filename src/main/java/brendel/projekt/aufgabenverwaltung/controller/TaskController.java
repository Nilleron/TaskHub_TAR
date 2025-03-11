package brendel.projekt.aufgabenverwaltung.controller;

import brendel.projekt.aufgabenverwaltung.models.Department;
import brendel.projekt.aufgabenverwaltung.models.Status;
import brendel.projekt.aufgabenverwaltung.models.Task;
import brendel.projekt.aufgabenverwaltung.services.EmailService;
import brendel.projekt.aufgabenverwaltung.services.ValidationService;
import brendel.projekt.aufgabenverwaltung.services.repositories.DepartmentRepository;
import brendel.projekt.aufgabenverwaltung.services.repositories.MyUserRepository;
import brendel.projekt.aufgabenverwaltung.services.repositories.ProjectRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import brendel.projekt.aufgabenverwaltung.services.repositories.TaskRepository;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(path = "/tasks")
public class TaskController {

    @Autowired
    private MyUserRepository myUserRepository;

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private EmailService emailService;

    @GetMapping({"", "/"})
    public String tasks(Model model, Principal principal) {

        model.addAttribute("user", myUserRepository.findUserByUsername(principal.getName()));
        model.addAttribute("tasks", taskRepository.findAllByOrderByUpdatedAtDesc());

        model.addAttribute("status", Status.values());
        model.addAttribute("today", new Date(System.currentTimeMillis()));
        List<Department> departments = departmentRepository.findAllByOrderByNameAsc();
        model.addAttribute("departments", departments);
        for (Department department : departments) {
            model.addAttribute("project" + department.getName(), projectRepository.findAllByDepartmentOrderByTitleAsc(department));
            model.addAttribute("user" + department.getName(), myUserRepository.findAllByDepartmentAndRolePriorityBeforeOrderByUsernameAsc(department, 101));
        }
        if(!model.containsAttribute("newTask")) {
            model.addAttribute("newTask", new Task());
        }

        return "tasks/index";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("newTask") Task newTask, RedirectAttributes redirectAttributes) throws MessagingException {

        if (newTask.getEstimatedDays() == null) {
            newTask.setEstimatedDays(0.0f);
        }

        Map<String, String> validationErrors = validationService.taskValidation(newTask);
        List<String> errorValues = new ArrayList<>(validationErrors.values());
        boolean titleError = validationErrors.containsKey("title");
        boolean estimatedDaysError = validationErrors.containsKey("estimatedDays");
        boolean assigneeError = validationErrors.containsKey("assignedTo");

        if (taskRepository.findTaskByTitle(newTask.getTitle()).isPresent()) {
            titleError = true;
            errorValues.add("Task title already exists");
        }

        if (!errorValues.isEmpty()) {
            redirectAttributes.addFlashAttribute("newTask", newTask);
            redirectAttributes.addFlashAttribute("titleError", titleError);
            redirectAttributes.addFlashAttribute("estimatedDaysError", estimatedDaysError);
            redirectAttributes.addFlashAttribute("assigneeError", assigneeError);
            redirectAttributes.addFlashAttribute("createErrors", errorValues);

            return "redirect:/tasks";
        }

        taskRepository.logUpdate(newTask);
        redirectAttributes.addFlashAttribute("message", "Task created successfully");
        if (newTask.getAssignedTo().getEmail() != null) {
            emailService.sendTaskNew(newTask);
        }

        return "redirect:/tasks";
    }

    @GetMapping("/delete")
    public String deleteTask(@RequestParam Integer id, RedirectAttributes redirectAttributes) {

        Task task = taskRepository.findTaskById(id);
        taskRepository.logDelete(task);

        redirectAttributes.addFlashAttribute("message", "Task deleted successfully");
        return "redirect:/tasks";
    }

    @GetMapping("/edit")
    public String editTask(Model model, Principal principal, @RequestParam Integer id) {

        Task editTask = taskRepository.findTaskById(id);
        Date today = new Date(System.currentTimeMillis());
        if (editTask.getDueDate().before(today)) {
            model.addAttribute("minDate", editTask.getDueDate());
        }
        else {
            model.addAttribute("minDate", today);
        }

        model.addAttribute("status", Status.values());
        model.addAttribute("today", today);
        model.addAttribute("departments", departmentRepository.findAllByOrderByNameAsc());
        for (Department department : departmentRepository.findAllByOrderByNameAsc()) {
            model.addAttribute("project" + department.getName(), projectRepository.findAllByDepartmentOrderByTitleAsc(department));
            model.addAttribute("user" + department.getName(), myUserRepository.findAllByDepartmentAndRolePriorityBeforeOrderByUsernameAsc(department, 101));
        }

        model.addAttribute("oldTask", editTask);
        if (!model.containsAttribute("editTask")) {
            model.addAttribute("editTask", editTask);
        }

        model.addAttribute("user", myUserRepository.findUserByUsername(principal.getName()));

        return "tasks/edit";
    }

    @PostMapping("/edit")
    public String editTask(@ModelAttribute("editTask") Task editTask, RedirectAttributes redirectAttributes) throws MessagingException {

        if (editTask.getEstimatedDays() == null) {
            editTask.setEstimatedDays(0.0f);
        }

        Map<String, String> validationErrors = validationService.taskValidation(editTask);
        List<String> errorValues = new ArrayList<>(validationErrors.values());
        boolean titleError = validationErrors.containsKey("title");
        boolean estimatedDaysError = validationErrors.containsKey("estimatedDays");
        boolean assigneeError = validationErrors.containsKey("assignedTo");

        Task oldTask = taskRepository.findTaskById(editTask.getId());

        if (taskRepository.findTaskByTitle(editTask.getTitle()).isPresent() &&
                !editTask.getTitle().equals(oldTask.getTitle())) {
            titleError = true;
            errorValues.add("Task title already exists");
        }

        if (editTask.equals(oldTask)) {
            errorValues.add("No Changes made");
        }

        if (!errorValues.isEmpty()) {
            redirectAttributes.addFlashAttribute("editTask", editTask);
            redirectAttributes.addFlashAttribute("titleError", titleError);
            redirectAttributes.addFlashAttribute("estimatedDaysError", estimatedDaysError);
            redirectAttributes.addFlashAttribute("assigneeError", assigneeError);
            redirectAttributes.addFlashAttribute("editErrors", errorValues);
            redirectAttributes.addAttribute("id", editTask.getId());
            return "redirect:/tasks/edit";
        }

        taskRepository.logUpdate(editTask);
        redirectAttributes.addFlashAttribute("message", "Task edited successfully");
        if (editTask.getAssignedTo().getEmail() != null) {
            emailService.sendTaskUpdate(editTask);
        }

        return "redirect:/tasks";
    }
}
