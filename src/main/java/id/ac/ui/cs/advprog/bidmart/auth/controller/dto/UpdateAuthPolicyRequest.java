package id.ac.ui.cs.advprog.bidmart.auth.controller.dto;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateAuthPolicyRequest(
    @Min(1) int maxConcurrentSessions,
    @NotNull AuthProperties.ConcurrentSessionPolicy concurrentSessionPolicy,
    @Min(1) int loginAttemptLimit,
    @Min(1) long loginAttemptWindowSeconds,
    @Min(1) int otpAttemptLimit,
    @Min(1) long otpTtlSeconds
) {
}
