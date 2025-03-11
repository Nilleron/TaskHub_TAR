package brendel.projekt.aufgabenverwaltung.controller;

import brendel.projekt.aufgabenverwaltung.models.Department;
import brendel.projekt.aufgabenverwaltung.models.Project;
import brendel.projekt.aufgabenverwaltung.models.Status;
import brendel.projekt.aufgabenverwaltung.services.EmailService;
import brendel.projekt.aufgabenverwaltung.services.ValidationService;
import brendel.projekt.aufgabenverwaltung.services.repositories.DepartmentRepository;
import brendel.projekt.aufgabenverwaltung.services.repositories.MyUserRepository;
import brendel.projekt.aufgabenverwaltung.services.repositories.ProjectRepository;
import brendel.projekt.aufgabenverwaltung.services.repositories.TaskRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MyUserRepository myUserRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ValidationService validationService;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private EmailService emailService;

    @GetMapping({"", "/"})
    public String projects(Model model, Principal principal) {

        model.addAttribute("user", myUserRepository.findUserByUsername(principal.getName()));
        model.addAttribute("projects", projectRepository.findAllByOrderByUpdatedAtDesc());

        model.addAttribute("status", Status.values());
        model.addAttribute("today", new Date(System.currentTimeMillis()));
        List<Department> departments = departmentRepository.findAllByOrderByNameAsc();
        model.addAttribute("departments", departments);
        for (Department department : departments) {
            model.addAttribute("user" + department.getName(), myUserRepository.findAllByDepartmentAndRolePriorityAfterOrderByUsernameAsc(department, 599));
        }
        if (!model.containsAttribute("newProject")) {
            model.addAttribute("newProject", new Project());
        }

        return "projects/index";
    }

    @PostMapping("/create")
    public String createProject(@ModelAttribute("newProject") Project newProject, RedirectAttributes redirectAttributes) throws MessagingException {

        newProject.setDepartment(newProject.getOwner().getDepartment());
        if (newProject.getEstimatedDays() == null) {
            newProject.setEstimatedDays(0.0f);
        }

        Map<String, String> validationErrors = validationService.projectValidation(newProject);
        List<String> errorValues = new ArrayList<>(validationErrors.values());
        boolean titleError = validationErrors.containsKey("title");
        boolean estimatedDaysError = validationErrors.containsKey("estimatedDays");

        if (projectRepository.findProjectByTitle(newProject.getTitle()).isPresent()) {
            titleError = true;
            errorValues.add("Project title already exists");
        }

        if (!errorValues.isEmpty()) {
            redirectAttributes.addFlashAttribute("newProject", newProject);
            redirectAttributes.addFlashAttribute("titleError", titleError);
            redirectAttributes.addFlashAttribute("estimatedDaysError", estimatedDaysError);
            redirectAttributes.addFlashAttribute("createErrors", errorValues);
            return "redirect:/projects";
        }

        projectRepository.logUpdate(newProject);
        redirectAttributes.addFlashAttribute("message", "Project created successfully");
        if (newProject.getOwner().getEmail() != null) {
            emailService.sendProjectNew(newProject);
        }

        return "redirect:/projects";
    }

    @GetMapping("/delete")
    public String deleteProject(@RequestParam Integer id, RedirectAttributes redirectAttributes) {

        taskRepository.deleteAll(taskRepository.findAllByProjectId(id));

        Project project = projectRepository.findProjectById(id);
        projectRepository.logDelete(project);

        redirectAttributes.addFlashAttribute("message", "Project with Tasks deleted successfully");

        return "redirect:/projects";
    }

    @GetMapping("/edit")
    public String editProject(Model model, Principal principal, @RequestParam Integer id) {

        Project editProject = projectRepository.findProjectById(id);
        Date today = new Date(System.currentTimeMillis());
        if (editProject.getDueDate().before(today)) {
            model.addAttribute("minDate", editProject.getDueDate());
        }
        else {
            model.addAttribute("minDate", today);
        }

        model.addAttribute("status", Status.values());
        model.addAttribute("today", today);
        List<Department> departments = departmentRepository.findAllByOrderByNameAsc();
        model.addAttribute("departments", departments);
        for (Department department : departments) {
            model.addAttribute("user" + department.getName(), myUserRepository.findAllByDepartmentAndRolePriorityAfterOrderByUsernameAsc(department, 599));
        }

        model.addAttribute("oldProject", editProject);
        if (!model.containsAttribute("editProject")) {
            model.addAttribute("editProject", editProject);
        }

        model.addAttribute("user", myUserRepository.findUserByUsername(principal.getName()));

        return "projects/edit";
    }

    @PostMapping("/edit")
    public String editProject(@ModelAttribute("editProject") Project editProject, RedirectAttributes redirectAttributes) throws MessagingException {

        editProject.setDepartment(editProject.getOwner().getDepartment());
        if (editProject.getEstimatedDays() == null) {
            editProject.setEstimatedDays(0.0f);
        }

        Map<String, String> validationErrors = validationService.projectValidation(editProject);
        List<String> errorValues = new ArrayList<>(validationErrors.values());
        boolean titleError = validationErrors.containsKey("title");
        boolean estimatedDaysError = validationErrors.containsKey("estimatedDays");
        boolean ownerError = false;

        Project oldProject = projectRepository.findProjectById(editProject.getId());

        if (projectRepository.findProjectByTitle(editProject.getTitle()).isPresent() &&
                !editProject.getTitle().equals(oldProject.getTitle())) {
            titleError = true;
            errorValues.add("Project title already exists");
        }

        if (!oldProject.getDepartment().equals(editProject.getDepartment()) &&
                !taskRepository.findAllByProjectId(editProject.getId()).isEmpty()) {
            ownerError = true;
            errorValues.add("Department of Project with Tasks cannot be changed");
        }

        if (editProject.equals(oldProject)) {
            errorValues.add("No Changes made");
        }

        if (!errorValues.isEmpty()) {
            redirectAttributes.addFlashAttribute("editProject", editProject);
            redirectAttributes.addFlashAttribute("titleError", titleError);
            redirectAttributes.addFlashAttribute("estimatedDaysError", estimatedDaysError);
            redirectAttributes.addFlashAttribute("ownerError", ownerError);
            redirectAttributes.addFlashAttribute("editErrors", errorValues);
            redirectAttributes.addAttribute("id", editProject.getId());
            return "redirect:/projects/edit";
        }

        projectRepository.logUpdate(editProject);
        redirectAttributes.addFlashAttribute("message", "Project edited successfully");
        if (editProject.getOwner().getEmail() != null) {
            emailService.sendProjectUpdate(editProject);
        }

        return "redirect:/projects";
    }
}
