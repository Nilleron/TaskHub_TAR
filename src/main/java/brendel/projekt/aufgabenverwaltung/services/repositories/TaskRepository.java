package brendel.projekt.aufgabenverwaltung.services.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import brendel.projekt.aufgabenverwaltung.models.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends CrudRepository<Task, Integer> {
    Logger log = LoggerFactory.getLogger(TaskRepository.class);

    Task findTaskById(Integer id);
    Optional<Task> findTaskByTitle(String title);
    List<Task> findAllByAssignedToId(Integer id);
    List<Task> findAllByAssignedToIdOrProjectOwnerIdOrderByStatusDescDueDateAscUpdatedAtDesc(Integer assignedToId, Integer ownerId);
    List<Task> findAllByOrderByUpdatedAtDesc();
    List<Task> findAllByProjectId(Integer id);

    default void logUpdate(Task task) {
        log.info("Task updated: {}", task);
        save(task);
    }

    default void logDelete(Task task) {
        log.warn("Task deleted: {}", task);
        delete(task);
    }
}
