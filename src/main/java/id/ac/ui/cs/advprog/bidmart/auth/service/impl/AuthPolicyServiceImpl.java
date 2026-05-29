package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPolicySettings;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthPolicySettingsRepository;
import id.ac.ui.cs.advprog.bidmart.auth.service.AuthPolicyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
public class AuthPolicyServiceImpl implements AuthPolicyService {
    private final AuthPolicySettingsRepository policyRepository;
    private final AuthProperties authProperties;

    public AuthPolicyServiceImpl(
        AuthPolicySettingsRepository policyRepository,
        AuthProperties authProperties
    ) {
        this.policyRepository = policyRepository;
        this.authProperties = authProperties;
    }

    @Override
    @Transactional
    public AuthPolicySettings getPolicy() {
        return policyRepository.findById(AuthPolicySettings.SINGLETON_ID)
            .orElseGet(this::createDefaultPolicy);
    }

    @Override
    @Transactional
    public AuthPolicySettings updatePolicy(
        int maxConcurrentSessions,
        AuthProperties.ConcurrentSessionPolicy concurrentSessionPolicy,
        int loginAttemptLimit,
        Duration loginAttemptWindow,
        int otpAttemptLimit,
        Duration otpTtl
    ) {
        validatePositive(maxConcurrentSessions, "Maximum concurrent sessions must be positive.");
        validatePositive(loginAttemptLimit, "Login attempt limit must be positive.");
        validatePositive(otpAttemptLimit, "OTP attempt limit must be positive.");
        validateDuration(loginAttemptWindow, "Login attempt window must be positive.");
        validateDuration(otpTtl, "OTP TTL must be positive.");
        if (concurrentSessionPolicy == null) {
            throw new IllegalArgumentException("Concurrent session policy is required.");
        }

        AuthPolicySettings settings = getPolicy();
        settings.setMaxConcurrentSessions(maxConcurrentSessions);
        settings.setConcurrentSessionPolicy(concurrentSessionPolicy);
        settings.setLoginAttemptLimit(loginAttemptLimit);
        settings.setLoginAttemptWindow(loginAttemptWindow);
        settings.setOtpAttemptLimit(otpAttemptLimit);
        settings.setOtpTtl(otpTtl);
        return policyRepository.save(settings);
    }

    private AuthPolicySettings createDefaultPolicy() {
        AuthPolicySettings settings = new AuthPolicySettings();
        settings.setMaxConcurrentSessions(authProperties.getMaxConcurrentSessions());
        settings.setConcurrentSessionPolicy(authProperties.getConcurrentSessionPolicy());
        settings.setLoginAttemptLimit(authProperties.getLoginAttemptLimit());
        settings.setLoginAttemptWindow(authProperties.getLoginAttemptWindow());
        settings.setOtpAttemptLimit(authProperties.getOtpAttemptLimit());
        settings.setOtpTtl(authProperties.getOtpTtl());
        return policyRepository.save(settings);
    }

    private void validatePositive(int value, String message) {
        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateDuration(Duration value, String message) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(message);
        }
    }
}
