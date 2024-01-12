package antifraud.entity;

import jakarta.persistence.*;
import org.hibernate.validator.constraints.CreditCardNumber;

@Entity
public class StolenCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @CreditCardNumber
    @Column(unique = true)
    private String number;

    public StolenCard() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
