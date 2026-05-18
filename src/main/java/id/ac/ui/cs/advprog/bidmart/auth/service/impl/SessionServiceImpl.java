package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import id.ac.ui.cs.advprog.bidmart.auth.exception.SessionLimitExceededException;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPolicySettings;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthSession;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthSessionRepository;
import id.ac.ui.cs.advprog.bidmart.auth.service.AuthPolicyService;
import id.ac.ui.cs.advprog.bidmart.auth.service.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
public class SessionServiceImpl implements SessionService {
    private final AuthSessionRepository authSessionRepository;
    private final AuthPolicyService authPolicyService;
    private final Clock clock;

    public SessionServiceImpl(
        AuthSessionRepository authSessionRepository,
        AuthPolicyService authPolicyService,
        Clock clock
    ) {
        this.authSessionRepository = authSessionRepository;
        this.authPolicyService = authPolicyService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void enforceLoginPolicy(AuthUser user) {
        Instant now = Instant.now(clock);
        AuthPolicySettings policy = authPolicyService.getPolicy();
        long activeSessions = authSessionRepository.countByUser_IdAndRevokedAtIsNullAndExpiresAtAfter(user.getId(), now);
        if (activeSessions < policy.getMaxConcurrentSessions()) {
            return;
        }

        if (policy.getConcurrentSessionPolicy() == AuthProperties.ConcurrentSessionPolicy.REJECT_NEW) {
            throw new SessionLimitExceededException();
        }

        long sessionsToRevoke = activeSessions - policy.getMaxConcurrentSessions() + 1;
        List<AuthSession> activeSessionsByAge =
            authSessionRepository.findByUser_IdAndRevokedAtIsNullAndExpiresAtAfterOrderByCreatedAtAsc(user.getId(), now);
        if (activeSessionsByAge.size() >= sessionsToRevoke) {
            activeSessionsByAge.stream()
                .limit(sessionsToRevoke)
                .forEach(session -> revokeSession(session, "Revoked due to concurrent session policy", now));
            return;
        }

        throw new SessionLimitExceededException();
    }

    @Override
    @Transactional
    public AuthSession createSession(AuthUser user, String ipAddress, String userAgent, Instant expiresAt) {
        AuthSession session = new AuthSession();
        session.setUser(user);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setExpiresAt(expiresAt);
        return authSessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthSession> findActiveById(UUID sessionId) {
        Instant now = Instant.now(clock);
        return authSessionRepository.findActiveByIdWithUser(sessionId)
            .filter(session -> session.getExpiresAt().isAfter(now));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthSession> listActiveSessions(UUID userId) {
        return authSessionRepository.findByUser_IdAndRevokedAtIsNullAndExpiresAtAfterOrderByCreatedAtAsc(
            userId,
            Instant.now(clock)
        );
    }

    @Override
    @Transactional
    public void revokeSession(AuthSession session, String reason, Instant revokedAt) {
        session.setRevokedAt(revokedAt);
        session.setRevokeReason(reason);
        authSessionRepository.save(session);
    }

    @Override
    @Transactional
    public void revokeOwnSession(UUID userId, UUID sessionId) {
        AuthSession session = authSessionRepository.findById(sessionId)
            .filter(candidate -> candidate.getUser().getId().equals(userId))
            .orElseThrow(() -> new id.ac.ui.cs.advprog.bidmart.auth.exception.ResourceNotFoundException("Session not found."));
        revokeSession(session, "Revoked by user", Instant.now(clock));
    }

    @Override
    @Transactional
    public void revokeAllSessionsForUser(UUID userId, String reason) {
        authSessionRepository.revokeAllActiveByUserId(userId, reason, Instant.now(clock));
    }
}
