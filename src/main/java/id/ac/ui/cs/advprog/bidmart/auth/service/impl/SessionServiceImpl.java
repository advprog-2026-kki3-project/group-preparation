package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import id.ac.ui.cs.advprog.bidmart.auth.exception.SessionLimitExceededException;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthSession;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthSessionRepository;
import id.ac.ui.cs.advprog.bidmart.auth.service.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionServiceImpl implements SessionService {
    private final AuthSessionRepository authSessionRepository;
    private final AuthProperties authProperties;
    private final Clock clock;

    public SessionServiceImpl(
        AuthSessionRepository authSessionRepository,
        AuthProperties authProperties,
        Clock clock
    ) {
        this.authSessionRepository = authSessionRepository;
        this.authProperties = authProperties;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void enforceLoginPolicy(AuthUser user) {
        long activeSessions = authSessionRepository.countByUser_IdAndRevokedAtIsNull(user.getId());
        if (activeSessions < authProperties.getMaxConcurrentSessions()) {
            return;
        }

        if (authProperties.getConcurrentSessionPolicy() == AuthProperties.ConcurrentSessionPolicy.REJECT_NEW) {
            throw new SessionLimitExceededException();
        }

        Optional<AuthSession> oldestActiveSession =
            authSessionRepository.findFirstByUser_IdAndRevokedAtIsNullOrderByCreatedAtAsc(user.getId());
        if (oldestActiveSession.isPresent()) {
            revokeSession(oldestActiveSession.get(), "Revoked due to concurrent session policy", Instant.now(clock));
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
        return authSessionRepository.findByIdAndRevokedAtIsNull(sessionId);
    }

    @Override
    @Transactional
    public void revokeSession(AuthSession session, String reason, Instant revokedAt) {
        session.setRevokedAt(revokedAt);
        session.setRevokeReason(reason);
        authSessionRepository.save(session);
    }
}
