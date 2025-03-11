package brendel.projekt.aufgabenverwaltung.services;

import brendel.projekt.aufgabenverwaltung.models.MyUser;
import brendel.projekt.aufgabenverwaltung.models.UserSettings;
import brendel.projekt.aufgabenverwaltung.services.repositories.MyUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class MyUserService {
    @Autowired
    MyUserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public void updatePassword(String username, String oldPassword, String newPassword) throws Exception {
        MyUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new Exception("Current password is incorrect");
        }

        if (newPassword.length() < 5) {
            throw new Exception("New password must be at least 6 characters");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.logUpdate(user);
    }

    public boolean existsUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public String saveUser(MyUser user) {
        String rawPassword = generatePassword(15);
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);

        if (user.getSettings() == null) {
            user = userRepository.findUserByUsername(user.getUsername());
            user.setSettings(new UserSettings());
            user.getSettings().setUser(user);
            userRepository.logUpdate(user);
        }

        return rawPassword;
    }

    public static String generatePassword(int len) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@$%^&*()-_=+[{]}\\|;:\'\",<.>/?";
        Random rnd = new Random();

        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(characters.charAt(rnd.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
