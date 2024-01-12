package antifraud.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

@Entity
public class IP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Pattern(regexp = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$",
    message = "Not a valid ip-address")
    @Column(unique = true)
    private String ip;

    public IP() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
