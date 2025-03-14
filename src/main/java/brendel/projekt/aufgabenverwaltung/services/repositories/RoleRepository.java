package brendel.projekt.aufgabenverwaltung.services.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import brendel.projekt.aufgabenverwaltung.models.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role, Integer> {
    Logger log = LoggerFactory.getLogger(RoleRepository.class);

    Role findRoleById(Integer id);
    Role findByName(String name);
    Role findTopByOrderByPriorityDesc();
    Optional<Role> findRoleByName(String name);
    Optional<Role> findRoleByPriority(Integer priority);
    List<Role> findAllByOrderByPriorityAsc();

    default void logUpdate(Role role) {
        log.info("Role updated: {}", role);
        save(role);
    }

    default void logDelete(Role role) {
        log.warn("Role deleted: {}", role);
        delete(role);
    }
}
