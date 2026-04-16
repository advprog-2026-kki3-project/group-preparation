package id.ac.ui.cs.advprog.bidmart.notification.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "notifications")
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String message;

    private Long orderId;

    @Column(nullable = false)
    private boolean read;

    @Column(nullable = false)
    private Instant createdAt;

    protected NotificationEntity() {
    }

    public NotificationEntity(String username, NotificationType type, String message, Long orderId) {
        this.username = username;
        this.type = type;
        this.message = message;
        this.orderId = orderId;
        this.read = false;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public NotificationType getType() { return type; }
    public String getMessage() { return message; }
    public Long getOrderId() { return orderId; }
    public boolean isRead() { return read; }
    public Instant getCreatedAt() { return createdAt; }

    public void markRead() {
        this.read = true;
    }
}