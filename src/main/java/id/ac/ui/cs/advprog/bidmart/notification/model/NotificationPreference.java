package id.ac.ui.cs.advprog.bidmart.notification.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {
    @Id
    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private boolean emailEnabled;

    @Column(nullable = false)
    private boolean pushEnabled;

    @Column(nullable = false)
    private Instant updatedAt;

    protected NotificationPreference() {
    }

    public NotificationPreference(String username) {
        this.username = username;
        this.emailEnabled = true;
        this.pushEnabled = true;
        this.updatedAt = Instant.now();
    }

    public String getUsername() {
        return username;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public boolean isPushEnabled() {
        return pushEnabled;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(boolean emailEnabled, boolean pushEnabled, Instant updatedAt) {
        this.emailEnabled = emailEnabled;
        this.pushEnabled = pushEnabled;
        this.updatedAt = updatedAt;
    }
}
