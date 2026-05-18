package id.ac.ui.cs.advprog.bidmart.auth.service;

import java.util.UUID;

public interface TwoFactorChallengeAttemptService {
    void recordFailedAttempt(UUID challengeId);
}
