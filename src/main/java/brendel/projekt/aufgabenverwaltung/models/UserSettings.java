package brendel.projekt.aufgabenverwaltung.models;

import jakarta.persistence.*;

import java.util.Optional;

@Entity
@Table(name = "settings")
public class UserSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settings_id")
    private Integer id;

    @Column(name = "darkmode")
    private Boolean darkmode = false;
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private MyUser user;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getDarkmode() {
        return darkmode;
    }

    public void setDarkmode(Boolean darkmode) {
        this.darkmode = darkmode;
    }

    public MyUser getUser() {
        return user;
    }

    public void setUser(MyUser user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "id=" + id +
                ", darkmode=" + darkmode +
                ", user=" + Optional.ofNullable(user).map(MyUser::getUsername).orElse("null") +
                '}';
    }
}
