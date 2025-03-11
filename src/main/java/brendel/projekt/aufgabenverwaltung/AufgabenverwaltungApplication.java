package brendel.projekt.aufgabenverwaltung;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

@SpringBootApplication
public class AufgabenverwaltungApplication {

    public static void main(String[] args) {
        SpringApplication.run(AufgabenverwaltungApplication.class, args);

        System.setProperty("java.awt.headless", "false");
        SwingUtilities.invokeLater(AufgabenverwaltungApplication::createTrayIcon);
    }

    private static void createTrayIcon() {

        if (!SystemTray.isSupported()) {
            System.err.println("System Tray wird nicht unterstützt!");
            return;
        }

        URL imageUrl = AufgabenverwaltungApplication.class.getResource("/images/icon.png");
        if (imageUrl == null) {
            System.err.println("Icon nicht gefunden!");
            return;
        }
        Image image = new ImageIcon(imageUrl).getImage();

        TrayIcon trayIcon = new TrayIcon(image, "TaskHub");
        trayIcon.setImageAutoSize(true);

        PopupMenu popup = new PopupMenu();

        MenuItem exitItem = new MenuItem("Beenden");
        exitItem.addActionListener(e -> {
            SystemTray.getSystemTray().remove(trayIcon);
            System.exit(0);
        });

        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            System.err.println("Fehler beim Hinzufügen des Tray-Icons: " + e.getMessage());
        }
    }
}
