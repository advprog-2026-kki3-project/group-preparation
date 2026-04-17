package id.ac.ui.cs.advprog.bidmart.wallet;

import jakarta.persistence.*;

@Entity
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private Long availableBalance = 0L;
    private Long heldBalance = 0L;

    public Wallet() {}
    public Wallet(String userId) { this.userId = userId; }

    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public Long getAvailableBalance() { return availableBalance; }
    public Long getHeldBalance() { return heldBalance; }

    public void addBalance(Long amount) {
        if (amount < 0 && this.availableBalance + amount < 0) {
            throw new IllegalArgumentException("Balance cannot become negative");
        }
        this.availableBalance += amount;
    }

    public void holdBalance(Long amount) {
        if (this.availableBalance < amount) {
            throw new IllegalStateException("Insufficient available balance to place this bid.");
        }
        this.availableBalance -= amount;
        this.heldBalance += amount;
    }

}