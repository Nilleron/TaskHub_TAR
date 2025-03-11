package brendel.projekt.aufgabenverwaltung.controller;

import brendel.projekt.aufgabenverwaltung.models.*;
import brendel.projekt.aufgabenverwaltung.services.EmailService;
import brendel.projekt.aufgabenverwaltung.services.MyUserService;
import brendel.projekt.aufgabenverwaltung.services.ValidationService;
import brendel.projekt.aufgabenverwaltung.services.repositories.*;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private MyUserRepository myUserRepository;

    @Autowired
    private MyUserService myUserService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping("/user")
    public String user(Model model, Principal principal) {
        model.addAttribute("user", myUserRepository.findUserByUsername(principal.getName()));
        model.addAttribute("roles", roleRepository.findAllByOrderByPriorityAsc());
        model.addAttribute("departments", departmentRepository.findAllByOrderByNameAsc());
        model.addAttribute("allUsers", myUserRepository.findAllByOrderByUsernameAsc());
        if (!model.containsAttribute("newUser")) {
            model.addAttribute("newUser", new MyUser());
        }

        return "admin/user/index";
    }

    @PostMapping("/user/add")
    public String addUser(@ModelAttribute("newUser") MyUser newUser, RedirectAttributes redirectAttributes) throws MessagingException {
        Map<String, String> validationErrors = validationService.userValidation(newUser);
        List<String> errorValues = new ArrayList<>(validationErrors.values());
        boolean nameError = validationErrors.containsKey("username");
        boolean emailError = validationErrors.containsKey("email");

        if (myUserService.existsUsername(newUser.getUsername())) {
            nameError = true;
            errorValues.add("Username already exists");
        }

        if (!errorValues.isEmpty()) {
            redirectAttributes.addFlashAttribute("newUser", newUser);
            redirectAttributes.addFlashAttribute("nameError", nameError);
            redirectAttributes.addFlashAttribute("emailError", emailError);
            redirectAttributes.addFlashAttribute("createErrors", errorValues);
            return "redirect:/admin/user";
        }

        String password = myUserService.saveUser(newUser);
        if (newUser.getEmail() != null) {
            emailService.sendNewUser(newUser, password);
        }
        redirectAttributes.addFlashAttribute("message", "Password for new User: " + password);
        return "redirect:/admin/user";
    }

    @GetMapping("/user/delete")
    public String deleteUser(@RequestParam Integer id, RedirectAttributes redirectAttributes) {

        MyUser user = myUserRepository.findUserById(id);
        if (user.getRole().getName().equals("admin")) {
            redirectAttributes.addFlashAttribute("error", "You cannot delete an admin user");
            return "redirect:/admin/user";
        }

        List<Project> projects = projectRepository.findAllByOwnerId(user.getId());
        for (Project project : projects) {
            project.setOwner(null);
            projectRepository.logUpdate(project);
        }
        List<Task> tasks = taskRepository.findAllByAssignedToId(user.getId());
        for (Task task : tasks) {
            task.setAssignedTo(null);
            taskRepository.logUpdate(task);
        }

        myUserRepository.logDelete(user);

        redirectAttributes.addFlashAttribute("message", "User deleted successfully");

        return "redirect:/admin/user";
    }

    @GetMapping("/user/edit")
    public String editUser(Model model, Principal principal, @RequestParam Integer id) {

        model.addAttribute("user", myUserRepository.findUserByUsername(principal.getName()));
        model.addAttribute("roles", roleRepository.findAllByOrderByPriorityAsc());
        model.addAttribute("departments", departmentRepository.findAllByOrderByNameAsc());

        MyUser editUser = myUserRepository.findUserById(id);
        model.addAttribute("oldUser", editUser);
        if (!model.containsAttribute("editUser")) {
            model.addAttribute("editUser", editUser);
        }

        return "admin/user/edit";
    }

    @PostMapping("/user/edit")
    public String editUser(@ModelAttribute("editUser") MyUser editUser, RedirectAttributes redirectAttributes) {

        Map<String, String> validationErrors = validationService.userValidation(editUser);
        List<String> errorValues = new ArrayList<>(validationErrors.values());
        boolean nameError = validationErrors.containsKey("username");
        boolean emailError = validationErrors.containsKey("email");
        boolean roleError = false;

        MyUser oldUser = myUserRepository.findUserById(editUser.getId());

        if (myUserRepository.findByUsername((editUser.getUsername())).isPresent() &&
                !editUser.getUsername().equals(oldUser.getUsername())) {
            nameError = true;
            errorValues.add("User already exists");
        }

        if (editUser.equals(oldUser)) {
            errorValues.add("No Changes made");
        }

        if (!oldUser.getRole().getPriority().equals(editUser.getRole().getPriority()) &&
                oldUser.getRole().getPriority().equals(999) &&
                myUserRepository.findAllByRolePriority(999).size() <= 1) {
            roleError = true;
            errorValues.add("You cannot edit last admin user");
        }

        if (!errorValues.isEmpty()) {
            redirectAttributes.addFlashAttribute("nameError", nameError);
            redirectAttributes.addFlashAttribute("emailError", emailError);
            redirectAttributes.addFlashAttribute("roleError", roleError);
            redirectAttributes.addFlashAttribute("editErrors", errorValues);
            redirectAttributes.addFlashAttribute("editUser", editUser);
            redirectAttributes.addAttribute("id", editUser.getId());
            return "redirect:/admin/user/edit";
        }

        if (!oldUser.getRole().getPriority().equals(editUser.getRole().getPriority()) &&
                oldUser.getRole().getPriority().equals(999) &&
                myUserRepository.findAllByRolePriority(999).size() <= 1) {
            redirectAttributes.addFlashAttribute("error", "You cannot edit last admin user");
            return "redirect:/admin/user/edit";
        }

        myUserRepository.logUpdate(editUser);
        redirectAttributes.addFlashAttribute("message", "User edited successfully");

        return "redirect:/admin/user";
    }

    @GetMapping("/user/reset")
    public String resetUserPassword(@RequestParam Integer id, RedirectAttributes redirectAttributes) throws MessagingException {

        MyUser user = myUserRepository.findUserById(id);
        String password = myUserService.saveUser(user);

        redirectAttributes.addFlashAttribute("message", "Password for new User: " + password);
        if (user.getEmail() != null) {
            emailService.sendPasswordReset(user, password);
        }

        return "redirect:/admin/user";
    }

    @GetMapping("/role")
    public String role(Model model, Principal principal) {

        model.addAttribute("user", myUserRepository.findUserByUsername(principal.getName()));
        model.addAttribute("roles", roleRepository.findAllByOrderByPriorityAsc());
        if (!model.containsAttribute("newRole")) {
            model.addAttribute("newRole", new Role());
        }

        return "admin/role/index";
    }

    @PostMapping("/role/add")
    public String addRole(Role newRole, RedirectAttributes redirectAttributes) {

        Map<String, String> validationErrors = validationService.roleValidation(newRole);
        List<String> errorValues = new ArrayList<>(validationErrors.values());
        boolean nameError = validationErrors.containsKey("name");
        boolean priorityError = validationErrors.containsKey("priority");

        if (roleRepository.findRoleByName(newRole.getName()).isPresent()) {
            nameError = true;
            errorValues.add("Role name already exists");
        }

        if (roleRepository.findRoleByPriority(newRole.getPriority()).isPresent()) {
            priorityError = true;
            errorValues.add("Role priority already exists");
        }

        if (!errorValues.isEmpty()) {
            redirectAttributes.addFlashAttribute("newRole", newRole);
            redirectAttributes.addFlashAttribute("nameError", nameError);
            redirectAttributes.addFlashAttribute("priorityError", priorityError);
            redirectAttributes.addFlashAttribute("createErrors", errorValues);
            return "redirect:/admin/role";
        }

        roleRepository.logUpdate(newRole);
        redirectAttributes.addFlashAttribute("message", "Role added successfully");

        return "redirect:/admin/role";
    }

    @GetMapping("/role/delete")
    public String deleteRole(@RequestParam Integer id, RedirectAttributes redirectAttributes) {

        Role role = roleRepository.findRoleById(id);
        Role maxPriority = roleRepository.findTopByOrderByPriorityDesc();
        if (role.equals(maxPriority)) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete highest priority role");
            return "redirect:/admin/role";
        }

        if (!myUserRepository.findAllByRoleId(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Role still has users");
            return "redirect:/admin/role";
        }

        roleRepository.logDelete(role);

        redirectAttributes.addFlashAttribute("message", "Role deleted successfully");

        return "redirect:/admin/role";
    }

    @GetMapping("/role/edit")
    public String editRole(Model model, Principal principal, @RequestParam Integer id, RedirectAttributes redirectAttributes) {

        Role editRole = roleRepository.findRoleById(id);
        Role maxPriority = roleRepository.findTopByOrderByPriorityDesc();
        if (editRole.equals(maxPriority)) {
            redirectAttributes.addFlashAttribute("error", "Cannot edit highest priority role");
            return "redirect:/admin/role";
        }

        model.addAttribute("user", myUserRepository.findUserByUsername(principal.getName()));
        model.addAttribute("oldRole", editRole);
        if (!model.containsAttribute("editRole")) {
            model.addAttribute("editRole", editRole);
        }

        return "admin/role/edit";
    }

    @PostMapping("/role/edit")
    public String editRole(@ModelAttribute("editRole") Role editRole, RedirectAttributes redirectAttributes) {

        Map<String, String> validationErrors = validationService.roleValidation(editRole);
        List<String> errorValues = new ArrayList<>(validationErrors.values());
        boolean nameError = validationErrors.containsKey("name");
        boolean priorityError = validationErrors.containsKey("priority");

        Role oldRole = roleRepository.findRoleById(editRole.getId());

        if (roleRepository.findRoleByName(editRole.getName()).isPresent() &&
                !editRole.getName().equals(oldRole.getName())) {
            nameError = true;
            errorValues.add("Role name already exists");
        }

        if (roleRepository.findRoleByPriority(editRole.getPriority()).isPresent() &&
                !editRole.getPriority().equals(oldRole.getPriority())) {
            priorityError = true;
            errorValues.add("Role priority already exists");
        }

        if (editRole.equals(oldRole)) {
            errorValues.add("No Changes made");
        }

        if (!errorValues.isEmpty()) {
            redirectAttributes.addFlashAttribute("nameError", nameError);
            redirectAttributes.addFlashAttribute("priorityError", priorityError);
            redirectAttributes.addFlashAttribute("editErrors", errorValues);
            redirectAttributes.addFlashAttribute("editRole", editRole);
            redirectAttributes.addAttribute("id", editRole.getId());
            return "redirect:/admin/role/edit";
        }

        roleRepository.logUpdate(editRole);
        redirectAttributes.addFlashAttribute("message", "Role edited successfully");

        return "redirect:/admin/role";
    }

    @GetMapping("/department")
    public String department(Model model, Principal principal) {

        model.addAttribute("user", myUserRepository.findUserByUsername(principal.getName()));
        model.addAttribute("departments", departmentRepository.findAllByOrderByNameAsc());
        if (!model.containsAttribute("newDepartment")) {
            model.addAttribute("newDepartment", new Department());
        }


        return "admin/department/index";
    }

    @PostMapping("/department/add")
    public String addDepartment(@ModelAttribute("newDepartment") Department newDepartment, RedirectAttributes redirectAttributes) {

        Map<String, String> validationErrors = validationService.departmentValidation(newDepartment);
        List<String> errorValues = new ArrayList<>(validationErrors.values());
        boolean nameError = validationErrors.containsKey("name");

        if (departmentRepository.findByName(newDepartment.getName()).isPresent()) {
            nameError = true;
            errorValues.add("Department name already exists");
        }

        if (!errorValues.isEmpty()) {
            redirectAttributes.addFlashAttribute("newDepartment", newDepartment);
            redirectAttributes.addFlashAttribute("nameError", nameError);
            redirectAttributes.addFlashAttribute("createErrors", errorValues);
            return "redirect:/admin/department";
        }

        departmentRepository.logUpdate(newDepartment);
        redirectAttributes.addFlashAttribute("message", "Department added successfully");

        return "redirect:/admin/department";
    }

    @GetMapping("/department/delete")
    public String deleteDepartment(@RequestParam Integer id, RedirectAttributes redirectAttributes) {

        if (!myUserRepository.findAllByDepartmentId(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Department still has users");
            return "redirect:/admin/department";
        }

        List<Project> projects = projectRepository.findAllByDepartmentId(id);
        for (Project project : projects) {
            project.setDepartment(null);
            projectRepository.logUpdate(project);
        }

        Department department = departmentRepository.findDepartmentById(id);
        departmentRepository.logDelete(department);

        redirectAttributes.addFlashAttribute("message", "Department deleted successfully");

        return "redirect:/admin/department";
    }

    @GetMapping("/department/edit")
    public String editDepartment(Model model, Principal principal, @RequestParam Integer id) {

        model.addAttribute("user", myUserRepository.findUserByUsername(principal.getName()));

        Department editDepartment = departmentRepository.findDepartmentById(id);
        model.addAttribute("oldDepartment", editDepartment);
        if (!model.containsAttribute("editDepartment")) {
            model.addAttribute("editDepartment", editDepartment);
        }

        return "admin/department/edit";
    }

    @PostMapping("/department/edit")
    public String editDepartment(@ModelAttribute("editDepartment") Department editDepartment, RedirectAttributes redirectAttributes) {

        Map<String, String> validationErrors = validationService.departmentValidation(editDepartment);
        List<String> errorValues = new ArrayList<>(validationErrors.values());
        boolean nameError = validationErrors.containsKey("name");

        Department oldDepartment = departmentRepository.findDepartmentById(editDepartment.getId());

        if (departmentRepository.findByName(editDepartment.getName()).isPresent() &&
                !editDepartment.getName().equals(oldDepartment.getName())) {
            nameError = true;
            errorValues.add("Department name already exists");
        }

        if (editDepartment.equals(oldDepartment)) {
            errorValues.add("No Changes made");
        }

        if (!errorValues.isEmpty()) {
            redirectAttributes.addFlashAttribute("nameError", nameError);
            redirectAttributes.addFlashAttribute("editErrors", errorValues);
            redirectAttributes.addFlashAttribute("editDepartment", editDepartment);
            redirectAttributes.addAttribute("id", editDepartment.getId());
            return "redirect:/admin/department/edit";
        }

        departmentRepository.logUpdate(editDepartment);
        redirectAttributes.addFlashAttribute("message", "Department edited successfully");

        return "redirect:/admin/department";
    }
}
