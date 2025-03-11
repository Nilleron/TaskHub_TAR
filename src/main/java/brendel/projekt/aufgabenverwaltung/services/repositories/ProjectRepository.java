package brendel.projekt.aufgabenverwaltung.services.repositories;

import brendel.projekt.aufgabenverwaltung.models.Department;
import brendel.projekt.aufgabenverwaltung.models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends CrudRepository<Project, Integer> {
    Logger log = LoggerFactory.getLogger(ProjectRepository.class);

    Project findProjectById(Integer id);
    Optional<Project> findProjectByTitle(String title);
    List<Project> findAllByOwnerId(Integer id);
    List<Project> findAllByOwnerIdOrderByStatusDescDueDateAscUpdatedAtDesc(Integer id);
    List<Project> findAllByOrderByUpdatedAtDesc();
    List<Project> findAllByDepartmentId(Integer id);
    List<Project> findAllByDepartmentOrderByTitleAsc(Department department);

    default void logUpdate(Project project) {
        log.info("Project updated: {}", project);
        save(project);
    }

    default void logDelete(Project project) {
        log.warn("Project deleted: {}", project);
        delete(project);
    }
}
