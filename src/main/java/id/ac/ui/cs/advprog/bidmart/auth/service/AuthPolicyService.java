package id.ac.ui.cs.advprog.bidmart.auth.service;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPolicySettings;

import java.time.Duration;

public interface AuthPolicyService {
    AuthPolicySettings getPolicy();

    AuthPolicySettings updatePolicy(
        int maxConcurrentSessions,
        id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties.ConcurrentSessionPolicy concurrentSessionPolicy,
        int loginAttemptLimit,
        Duration loginAttemptWindow,
        int otpAttemptLimit,
        Duration otpTtl
    );
}
