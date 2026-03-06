package id.ac.ui.cs.advprog.bidmart.auth.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {
    @NotNull
    private Duration accessTokenTtl;

    @NotNull
    private Duration refreshTokenTtl;

    private boolean refreshRotationEnabled;
    private boolean refreshReuseDetectionEnabled;

    @Min(1)
    private int maxConcurrentSessions;

    @NotNull
    private ConcurrentSessionPolicy concurrentSessionPolicy;

    @Min(1)
    private int loginAttemptLimit;

    @NotNull
    private Duration loginAttemptWindow;

    @Min(1)
    private int otpAttemptLimit;

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }

    public Duration getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    public void setRefreshTokenTtl(Duration refreshTokenTtl) {
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public boolean isRefreshRotationEnabled() {
        return refreshRotationEnabled;
    }

    public void setRefreshRotationEnabled(boolean refreshRotationEnabled) {
        this.refreshRotationEnabled = refreshRotationEnabled;
    }

    public boolean isRefreshReuseDetectionEnabled() {
        return refreshReuseDetectionEnabled;
    }

    public void setRefreshReuseDetectionEnabled(boolean refreshReuseDetectionEnabled) {
        this.refreshReuseDetectionEnabled = refreshReuseDetectionEnabled;
    }

    public int getMaxConcurrentSessions() {
        return maxConcurrentSessions;
    }

    public void setMaxConcurrentSessions(int maxConcurrentSessions) {
        this.maxConcurrentSessions = maxConcurrentSessions;
    }

    public ConcurrentSessionPolicy getConcurrentSessionPolicy() {
        return concurrentSessionPolicy;
    }

    public void setConcurrentSessionPolicy(ConcurrentSessionPolicy concurrentSessionPolicy) {
        this.concurrentSessionPolicy = concurrentSessionPolicy;
    }

    public int getLoginAttemptLimit() {
        return loginAttemptLimit;
    }

    public void setLoginAttemptLimit(int loginAttemptLimit) {
        this.loginAttemptLimit = loginAttemptLimit;
    }

    public Duration getLoginAttemptWindow() {
        return loginAttemptWindow;
    }

    public void setLoginAttemptWindow(Duration loginAttemptWindow) {
        this.loginAttemptWindow = loginAttemptWindow;
    }

    public int getOtpAttemptLimit() {
        return otpAttemptLimit;
    }

    public void setOtpAttemptLimit(int otpAttemptLimit) {
        this.otpAttemptLimit = otpAttemptLimit;
    }

    public enum ConcurrentSessionPolicy {
        REJECT_NEW,
        REVOKE_OLDEST
    }
}
