package brendel.projekt.aufgabenverwaltung.services;

import brendel.projekt.aufgabenverwaltung.models.MyUser;
import brendel.projekt.aufgabenverwaltung.models.Project;
import brendel.projekt.aufgabenverwaltung.models.Task;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    SpringTemplateEngine templateEngine;

    Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${spring.mail.username}")
    private String emailFrom;
    @Value("${app.mail.sender.name}")
    private String senderName;

    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendEmail(String to, String cc, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        try {
            helper.setFrom(new InternetAddress(emailFrom, senderName));
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }

        helper.setTo(to);
        if (cc != null) {
            helper.setCc(cc);
        }
        helper.setSubject("TaskHub - " + subject);
        helper.setText(text, true);

        log.info("Sending email to {} with subject {}", to, subject);

        mailSender.send(message);
    }

    public void sendHtmlEmail(String to, String cc, String subject, String template, Map<String, Object> templateModel) throws MessagingException {
        Context context = new Context();
        context.setVariables(templateModel);
        String html = templateEngine.process(template, context);
        sendEmail(to, cc, subject, html);
    }

    public void sendNewUser(MyUser user, String password) throws MessagingException {
        Map<String, Object> model = new HashMap<>();
        String title = "Your User";
        if (user.getFirstName() != null) {
            model.put("name", user.getFirstName());
        }
        else {
            model.put("name", user.getUsername());
        }
        model.put("username", user.getUsername());
        model.put("password", password);

        sendHtmlEmail(user.getEmail(), null, title, "email/newUser", model);
    }

    public void sendPasswordReset(MyUser user, String password) throws MessagingException {
        Map<String, Object> model = new HashMap<>();
        String title = "New Password";
        if (user.getFirstName() != null) {
            model.put("name", user.getFirstName());
        }
        else {
            model.put("name", user.getUsername());
        }
        model.put("password", password);

        sendHtmlEmail(user.getEmail(), null, title, "email/passwordReset" ,model);
    }

    public void sendTaskUpdate(Task task) throws MessagingException {
        Map<String, Object> model = new HashMap<>();
        String title = "Task Update";
        model.put("title", title);
        MyUser user = task.getAssignedTo();
        if (user.getFirstName() != null) {
            model.put("name", user.getFirstName());
        }
        else {
            model.put("name", user.getUsername());
        }
        model.put("body", "There is an update for your task:");
        model.put("task", task);

        sendHtmlEmail(user.getEmail(), task.getProject().getOwner().getEmail(), title, "email/taskUpdate", model);
    }

    public void sendTaskNew(Task task) throws MessagingException {
        Map<String, Object> model = new HashMap<>();
        String title = "New Task";
        model.put("title", title);
        MyUser user = task.getAssignedTo();
        if (user.getFirstName() != null) {
            model.put("name", user.getFirstName());
        }
        else {
            model.put("name", user.getUsername());
        }
        model.put("body", "There is a new task for you:");
        model.put("task", task);

        sendHtmlEmail(user.getEmail(), task.getProject().getOwner().getEmail(), title, "email/taskUpdate", model);
    }

    public void sendTaskCompleted(Task task) throws MessagingException {
        Map<String, Object> model = new HashMap<>();
        String title = "Task Completed";
        model.put("title", title);
        MyUser user = task.getAssignedTo();
        if (user.getFirstName() != null) {
            model.put("name", user.getFirstName());
        }
        else {
            model.put("name", user.getUsername());
        }
        model.put("body", "a task has been completed for your project:");
        model.put("task", task);

        sendHtmlEmail(user.getEmail(), task.getProject().getOwner().getEmail(), title, "email/taskUpdate", model);
    }

    public void sendProjectUpdate(Project project) throws MessagingException {
        Map<String, Object> model = new HashMap<>();
        String title = "Project Update";
        model.put("title", title);
        MyUser user = project.getOwner();
        if (user.getFirstName() != null) {
            model.put("name", user.getFirstName());
        }
        else {
            model.put("name", user.getUsername());
        }
        model.put("body", "There is an update for your project:");
        model.put("project", project);

        sendHtmlEmail(user.getEmail(), null, title, "email/projectUpdate", model);
    }

    public void sendProjectNew(Project project) throws MessagingException {
        Map<String, Object> model = new HashMap<>();
        String title = "New Project";
        model.put("title", title);
        MyUser user = project.getOwner();
        if (user.getFirstName() != null) {
            model.put("name", user.getFirstName());
        }
        else {
            model.put("name", user.getUsername());
        }
        model.put("body", "There is a new project for you:");
        model.put("project", project);

        sendHtmlEmail(user.getEmail(), null, title, "email/projectUpdate", model);
    }
}
