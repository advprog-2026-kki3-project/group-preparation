package id.ac.ui.cs.advprog.bidmart.auth.controller.dto;

import java.time.Instant;

public record AuthPolicyResponse(
    int maxConcurrentSessions,
    String concurrentSessionPolicy,
    int loginAttemptLimit,
    long loginAttemptWindowSeconds,
    int otpAttemptLimit,
    long otpTtlSeconds,
    Instant updatedAt
) {
}
