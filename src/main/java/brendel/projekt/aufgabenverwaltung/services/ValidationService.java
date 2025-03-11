package brendel.projekt.aufgabenverwaltung.services;

import brendel.projekt.aufgabenverwaltung.models.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ValidationService {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern pattern = Pattern.compile(EMAIL_REGEX);

    public boolean isValid(String email) {
        if (email == null) return false;
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public Map<String, String> userValidation(MyUser user) {
        Map<String, String> errors = new HashMap<>();
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            errors.put("username", "Username is required");
        }

        if (!isValid(user.getEmail())) {
            errors.put("email", "Email is invalid");
        }

        return errors;
    }

    public Map<String, String> roleValidation(Role role) {
        Map<String, String> errors = new HashMap<>();
        if (role.getName() == null || role.getName().isEmpty()) {
            errors.put("name", "Role name is required");
        }
        if (role.getPriority() == null) {
            errors.put("priority", "Priority is required");
        }
        else {
            if (role.getPriority() < 0 || role.getPriority() > 999) {
                errors.put("priority", "Priority must be between 0 and 999");
            }
        }
        return errors;
    }

    public Map<String, String> departmentValidation(Department department) {
        Map<String, String> errors = new HashMap<>();
        if (department.getName() == null || department.getName().isEmpty()) {
            errors.put("name", "Department name is required");
        }

        return errors;
    }

    public Map<String, String> projectValidation(Project project) {
        Map<String, String> errors = new HashMap<>();
        if (project.getTitle() == null || project.getTitle().isEmpty()) {
            errors.put("title", "Project title is required");
        }
        if (project.getEstimatedDays() != null && project.getEstimatedDays() < 0) {
            errors.put("estimatedDays", "Estimated days must be a positive number");
        }

        return errors;
    }

    public Map<String, String> taskValidation(Task task) {
        Map<String, String> errors = new HashMap<>();
        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            errors.put("title", "Task title is required");
        }
        if (task.getEstimatedDays() != null && task.getEstimatedDays() < 0) {
            errors.put("estimatedDays", "Estimated days must be a positive number");
        }
        if (!task.getAssignedTo().getDepartment().equals(task.getProject().getDepartment())) {
            errors.put("assignedTo", "Assigned to different department");
        }

        return errors;
    }
}
