package brendel.projekt.aufgabenverwaltung.services.repositories;

import brendel.projekt.aufgabenverwaltung.models.UserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;

public interface SettingsRepository extends CrudRepository<UserSettings, Integer> {
    Logger log = LoggerFactory.getLogger(SettingsRepository.class);

    default void logUpdate(UserSettings settings) {
        log.info("Settings updated: {}", settings);
        save(settings);
    }

    default void logDelete(UserSettings settings) {
        log.warn("Settings deleted: {}", settings);
        delete(settings);
    }
}
