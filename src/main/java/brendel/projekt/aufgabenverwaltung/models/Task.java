package brendel.projekt.aufgabenverwaltung.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "description")
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.open;
    @Column(name = "due_date")
    private Date dueDate = new Date(System.currentTimeMillis());
    @Column(name = "estimated_days")
    private Float estimatedDays = 0.0f;
    @Column(name = "actual_days")
    private Float actualDays = 0.0f;
    @Column(name = "created_at")
    @CreationTimestamp
    private Timestamp createdAt;
    @Column(name = "updated_at")
    @UpdateTimestamp
    private Timestamp updatedAt;
    @ManyToOne()
    @JoinColumn(name = "project_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Project project;
    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private MyUser assignedTo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date due_date) {
        this.dueDate = due_date;
    }

    public Float getEstimatedDays() {
        return estimatedDays;
    }

    public void setEstimatedDays(Float estimated_days) {
        this.estimatedDays = estimated_days;
    }

    public Float getActualDays() {
        return actualDays;
    }

    public void setActualDays(Float actual_days) {
        this.actualDays = actual_days;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp created_at) {
        this.createdAt = created_at;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updated_at) {
        this.updatedAt = updated_at;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public MyUser getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(MyUser assigned_to) {
        this.assignedTo = assigned_to;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id) && Objects.equals(title, task.title) && status == task.status && Objects.equals(dueDate, task.dueDate) && Objects.equals(estimatedDays, task.estimatedDays) && Objects.equals(actualDays, task.actualDays) && Objects.equals(project, task.project) && Objects.equals(assignedTo, task.assignedTo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", dueDate=" + dueDate +
                ", estimatedDays=" + estimatedDays +
                ", actualDays=" + actualDays +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", project=" + Optional.ofNullable(project).map(Project::getTitle).orElse("null") +
                ", assignedTo=" + Optional.ofNullable(assignedTo).map(MyUser::getUsername).orElse("null") +
                '}';
    }
}
