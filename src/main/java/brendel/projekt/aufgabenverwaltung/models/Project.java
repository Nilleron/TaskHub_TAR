package brendel.projekt.aufgabenverwaltung.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "project")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "description")
    private String description;
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
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private MyUser owner;
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    public Integer getId() {
        return id;
    }

    public void setId(Integer project_id) {
        this.id = project_id;
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

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Float getEstimatedDays() {
        return estimatedDays;
    }

    public void setEstimatedDays(Float estimatedDays) {
        this.estimatedDays = estimatedDays;
    }

    public Float getActualDays() {
        return actualDays;
    }

    public void setActualDays(Float actualDays) {
        this.actualDays = actualDays;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public MyUser getOwner() {
        return owner;
    }

    public void setOwner(MyUser owner) {
        this.owner = owner;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id) && Objects.equals(title, project.title) && status == project.status && Objects.equals(dueDate, project.dueDate) && Objects.equals(estimatedDays, project.estimatedDays) && Objects.equals(actualDays, project.actualDays) && Objects.equals(owner, project.owner) && Objects.equals(department, project.department);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", dueDate=" + dueDate +
                ", estimatedDays=" + estimatedDays +
                ", actualDays=" + actualDays +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", owner=" + Optional.ofNullable(owner).map(MyUser::getUsername).orElse("null") +
                ", department=" + Optional.ofNullable(department).map(Department::getName).orElse("null") +
                '}';
    }
}
