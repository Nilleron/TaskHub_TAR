package brendel.projekt.aufgabenverwaltung.controller;

import brendel.projekt.aufgabenverwaltung.models.MyUser;
import brendel.projekt.aufgabenverwaltung.models.Role;
import brendel.projekt.aufgabenverwaltung.models.UserSettings;
import brendel.projekt.aufgabenverwaltung.services.repositories.MyUserRepository;
import brendel.projekt.aufgabenverwaltung.services.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class LoginController {

    @Autowired
    MyUserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String handleLogin() {

        List<Role> roles = (List<Role>) roleRepository.findAll();
        if (roles.isEmpty()) {
            Role role = new Role();
            role.setName("admin");
            role.setPriority(999);
            roleRepository.logUpdate(role);
            role = new Role();
            role.setName("supervisor");
            role.setPriority(800);
            roleRepository.logUpdate(role);
            role = new Role();
            role.setName("manager");
            role.setPriority(600);
            roleRepository.logUpdate(role);
            role = new Role();
            role.setName("user");
            role.setPriority(100);
            roleRepository.logUpdate(role);
        }
        if (roleRepository.findRoleByName("admin").isEmpty()) {
            Role role = new Role();
            role.setName("admin");
            role.setPriority(999);
            roleRepository.logUpdate(role);
        }
        if (roleRepository.findRoleByName("supervisor").isEmpty()) {
            Role role = new Role();
            role.setName("supervisor");
            role.setPriority(800);
            roleRepository.logUpdate(role);
        }
        if (roleRepository.findRoleByName("manager").isEmpty()) {
            Role role = new Role();
            role.setName("manager");
            role.setPriority(600);
            roleRepository.logUpdate(role);
        }
        if (roleRepository.findRoleByName("user").isEmpty()) {
            Role role = new Role();
            role.setName("user");
            role.setPriority(100);
            roleRepository.logUpdate(role);
        }

        List<MyUser> users = (List<MyUser>) userRepository.findAll();
        if (users.isEmpty()) {
            MyUser user = new MyUser();
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("admin"));
            user.setRole(roleRepository.findByName("admin"));
            userRepository.logUpdate(user);

            if (user.getSettings() == null) {
                user = userRepository.findUserByUsername(user.getUsername());
                user.setSettings(new UserSettings());
                user.getSettings().setUser(user);
                userRepository.logUpdate(user);
            }
        }

        return "login/customLogin";
    }
}
