package brendel.projekt.aufgabenverwaltung.services.repositories;

import brendel.projekt.aufgabenverwaltung.models.Department;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import brendel.projekt.aufgabenverwaltung.models.MyUser;

import java.util.List;
import java.util.Optional;

public interface MyUserRepository extends CrudRepository<MyUser, Integer> {
    Logger log = LoggerFactory.getLogger(MyUserRepository.class);

    MyUser findUserById(Integer id);
    Optional<MyUser> findByUsername(String username);
    MyUser findUserByUsername(String username);
    List<MyUser> findAllByRoleId(Integer id);
    List<MyUser> findAllByRolePriority(Integer priority);
    List<MyUser> findAllByDepartmentId(Integer id);
    List<MyUser> findAllByOrderByUsernameAsc();
    List<MyUser> findAllByDepartmentAndRolePriorityBeforeOrderByUsernameAsc(Department department, Integer priority);
    List<MyUser> findAllByDepartmentAndRolePriorityAfterOrderByUsernameAsc(Department department, Integer priority);

    default void logUpdate(MyUser user) {
        log.info("User updated: {}", user);
        save(user);
    }

    default void logDelete(MyUser user) {
        log.warn("User deleted: {}", user);
        delete(user);
    }
}
