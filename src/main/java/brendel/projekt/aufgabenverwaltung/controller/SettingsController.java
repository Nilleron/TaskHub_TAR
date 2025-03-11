package brendel.projekt.aufgabenverwaltung.controller;

import brendel.projekt.aufgabenverwaltung.models.MyUser;
import brendel.projekt.aufgabenverwaltung.models.UserSettings;
import brendel.projekt.aufgabenverwaltung.services.MyUserService;
import brendel.projekt.aufgabenverwaltung.services.repositories.MyUserRepository;
import brendel.projekt.aufgabenverwaltung.services.repositories.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    @Autowired
    MyUserRepository myUserRepository;

    @Autowired
    SettingsRepository settingsRepository;

    @Autowired
    MyUserService myUserService;

    @GetMapping({"", "/"})
    public String index(Model model, Principal principal) {
        MyUser myUser = myUserRepository.findUserByUsername(principal.getName());
        if (myUser.getSettings() == null) {
            myUser.setSettings(new UserSettings());
            myUser.getSettings().setUser(myUser);
            myUserRepository.logUpdate(myUser);
        }
        model.addAttribute("settings", myUser.getSettings());
        model.addAttribute("user", myUser);
        return "settings";
    }

    @PostMapping({"", "/"})
    public String save(@ModelAttribute("settings") UserSettings settings) {
        settingsRepository.logUpdate(settings);
        return "redirect:/settings";
    }

    @PostMapping("/changepassword")
    public String changePassword(@RequestParam Integer userId, @RequestParam String currentPassword, @RequestParam String newPassword, @RequestParam String confirmPassword, RedirectAttributes redirectAttributes) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "New passwords do not match");
                return "redirect:/settings";
            }

            myUserService.updatePassword(myUserRepository.findUserById(userId).getUsername(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("message", "Password changed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/settings";
    }
}
