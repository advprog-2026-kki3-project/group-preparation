package id.ac.ui.cs.advprog.bidmart.auth.service;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthSession;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionService {
    void enforceLoginPolicy(AuthUser user);

    AuthSession createSession(AuthUser user, String ipAddress, String userAgent, Instant expiresAt, boolean twoFactorVerified);

    Optional<AuthSession> findActiveById(UUID sessionId);

    List<AuthSession> listActiveSessions(UUID userId);

    void revokeSession(AuthSession session, String reason, Instant revokedAt);

    void revokeOwnSession(UUID userId, UUID sessionId);

    void revokeAllSessionsForUser(UUID userId, String reason);

    void markTwoFactorVerified(UUID userId, UUID sessionId, boolean verified);
}
