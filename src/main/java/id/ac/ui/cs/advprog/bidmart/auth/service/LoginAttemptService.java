package id.ac.ui.cs.advprog.bidmart.auth.service;

import java.time.Instant;
import java.util.Optional;

public interface LoginAttemptService {
    long countFailedAttemptsSince(String email, Instant attemptedAfter);

    Optional<Instant> findOldestFailedAttemptSince(String email, Instant attemptedAfter);

    void recordAttempt(String email, String ipAddress, boolean successful);
}
