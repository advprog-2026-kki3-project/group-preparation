package id.ac.ui.cs.advprog.bidmart.auth.model;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "auth_policy_settings")
public class AuthPolicySettings {
    public static final String SINGLETON_ID = "default";

    @Id
    @Column(length = 32)
    private String id = SINGLETON_ID;

    @Column(name = "max_concurrent_sessions", nullable = false)
    private int maxConcurrentSessions;

    @Enumerated(EnumType.STRING)
    @Column(name = "concurrent_session_policy", nullable = false, length = 32)
    private AuthProperties.ConcurrentSessionPolicy concurrentSessionPolicy;

    @Column(name = "login_attempt_limit", nullable = false)
    private int loginAttemptLimit;

    @Column(name = "login_attempt_window_seconds", nullable = false)
    private long loginAttemptWindowSeconds;

    @Column(name = "otp_attempt_limit", nullable = false)
    private int otpAttemptLimit;

    @Column(name = "otp_ttl_seconds", nullable = false)
    private long otpTtlSeconds;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getId() {
        return id;
    }

    public int getMaxConcurrentSessions() {
        return maxConcurrentSessions;
    }

    public void setMaxConcurrentSessions(int maxConcurrentSessions) {
        this.maxConcurrentSessions = maxConcurrentSessions;
    }

    public AuthProperties.ConcurrentSessionPolicy getConcurrentSessionPolicy() {
        return concurrentSessionPolicy;
    }

    public void setConcurrentSessionPolicy(AuthProperties.ConcurrentSessionPolicy concurrentSessionPolicy) {
        this.concurrentSessionPolicy = concurrentSessionPolicy;
    }

    public int getLoginAttemptLimit() {
        return loginAttemptLimit;
    }

    public void setLoginAttemptLimit(int loginAttemptLimit) {
        this.loginAttemptLimit = loginAttemptLimit;
    }

    public Duration getLoginAttemptWindow() {
        return Duration.ofSeconds(loginAttemptWindowSeconds);
    }

    public void setLoginAttemptWindow(Duration loginAttemptWindow) {
        this.loginAttemptWindowSeconds = loginAttemptWindow.toSeconds();
    }

    public int getOtpAttemptLimit() {
        return otpAttemptLimit;
    }

    public void setOtpAttemptLimit(int otpAttemptLimit) {
        this.otpAttemptLimit = otpAttemptLimit;
    }

    public Duration getOtpTtl() {
        return Duration.ofSeconds(otpTtlSeconds);
    }

    public void setOtpTtl(Duration otpTtl) {
        this.otpTtlSeconds = otpTtl.toSeconds();
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
