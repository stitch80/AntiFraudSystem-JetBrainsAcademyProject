package antifraud.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Entity
public class AppUser{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private String name;
    @NotNull
    private String username;
    @NotNull
    private String password;

//    @OneToOne
//    @JoinColumn(name = "user_id", nullable = false)
    private SimpleGrantedAuthority role;

    private boolean isNonLocked = false;

    public AppUser() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SimpleGrantedAuthority getRole() {
        return role;
    }

    public String getRoleName() {
        return role.toString().substring(5);
    }

    public void setRole(SimpleGrantedAuthority role) {
        this.role = role;
    }

    public boolean isNonLocked() {
        return isNonLocked;
    }

    public void setNonLocked(boolean nonLocked) {
        this.isNonLocked = nonLocked;
    }
}
