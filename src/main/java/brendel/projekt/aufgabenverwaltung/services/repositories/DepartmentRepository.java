package brendel.projekt.aufgabenverwaltung.services.repositories;

import brendel.projekt.aufgabenverwaltung.models.Department;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends CrudRepository<Department, Integer> {
    Logger log = LoggerFactory.getLogger(DepartmentRepository.class);

    Department findDepartmentById(Integer id);
    Optional<Department> findByName(String name);
    List<Department> findAllByOrderByNameAsc();

    default void logUpdate(Department department) {
        log.info("Department updated: {}", department);
        save(department);
    }

    default void logDelete(Department department) {
        log.warn("Department deleted: {}", department);
        delete(department);
    }
}
