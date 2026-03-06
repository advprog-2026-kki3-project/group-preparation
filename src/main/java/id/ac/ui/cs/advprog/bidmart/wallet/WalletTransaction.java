package id.ac.ui.cs.advprog.bidmart.wallet;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String transactionType; // e.g., "TOP_UP", "HOLD", "WITHDRAW"
    private Long amount;
    private LocalDateTime timestamp;

    public WalletTransaction() {}
    public WalletTransaction(String userId, String transactionType, Long amount) {
        this.userId = userId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    public String getTransactionType() { return transactionType; }
    public Long getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
}