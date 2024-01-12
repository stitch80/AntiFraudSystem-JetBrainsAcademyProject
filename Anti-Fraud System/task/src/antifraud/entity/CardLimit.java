package antifraud.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

@Entity
public class CardLimit {

    public static final int ALLOW_LIMIT = 200;
    public static final int MANUAL_LIMIT = 1500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    private String cardNumber;

    @Positive
    private int allowLimit;

    @Positive
    private int manualLimit;

    public CardLimit() {
    }

    public CardLimit(String cardNumber, int allowLimit, int manualLimit) {
        this.cardNumber = cardNumber;
        this.allowLimit = allowLimit;
        this.manualLimit = manualLimit;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public int getAllowLimit() {
        return allowLimit;
    }

    public void setAllowLimit(int allowLimit) {
        this.allowLimit = allowLimit;
    }

    public int getManualLimit() {
        return manualLimit;
    }

    public void setManualLimit(int manualLimit) {
        this.manualLimit = manualLimit;
    }
}
