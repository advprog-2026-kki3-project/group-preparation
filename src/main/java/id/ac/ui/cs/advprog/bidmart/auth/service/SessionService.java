package id.ac.ui.cs.advprog.bidmart.auth.service;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthSession;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SessionService {
    void enforceLoginPolicy(AuthUser user);

    AuthSession createSession(AuthUser user, String ipAddress, String userAgent, Instant expiresAt);

    Optional<AuthSession> findActiveById(UUID sessionId);

    void revokeSession(AuthSession session, String reason, Instant revokedAt);
}
