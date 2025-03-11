package brendel.projekt.aufgabenverwaltung.controller;

import brendel.projekt.aufgabenverwaltung.models.MyUser;
import brendel.projekt.aufgabenverwaltung.services.repositories.MyUserRepository;
import brendel.projekt.aufgabenverwaltung.services.repositories.ProjectRepository;
import brendel.projekt.aufgabenverwaltung.services.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class ContentController {

    @Autowired
    private MyUserRepository myUserRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping({"", "/"})
    public String index(Model model, Principal principal) {
        MyUser myUser = myUserRepository.findUserByUsername(principal.getName());

        model.addAttribute("user", myUser);

        model.addAttribute("tasks", taskRepository.findAllByOrderByUpdatedAtDesc());
        model.addAttribute("projects", projectRepository.findAllByOrderByUpdatedAtDesc());

        return "index";
    }
}
