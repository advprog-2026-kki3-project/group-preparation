package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import id.ac.ui.cs.advprog.bidmart.auth.exception.ResourceNotFoundException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.SessionLimitExceededException;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPolicySettings;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthSession;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserStatus;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthSessionRepository;
import id.ac.ui.cs.advprog.bidmart.auth.service.AuthPolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {
    @Mock
    private AuthSessionRepository authSessionRepository;

    @Mock
    private AuthPolicyService authPolicyService;

    private SessionServiceImpl sessionService;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-03-06T12:00:00Z"), ZoneOffset.UTC);
        sessionService = new SessionServiceImpl(authSessionRepository, authPolicyService, clock);
    }

    @Test
    void enforceLoginPolicyRevokesOldestWhenLimitReached() {
        UUID userId = UUID.randomUUID();
        AuthUser user = new AuthUser();
        user.setPrimaryRole(UserRole.BUYER);
        setUserId(user, userId);

        AuthSession oldest = new AuthSession();
        AuthPolicySettings policy = new AuthPolicySettings();
        policy.setMaxConcurrentSessions(1);
        policy.setConcurrentSessionPolicy(AuthProperties.ConcurrentSessionPolicy.REVOKE_OLDEST);
        when(authPolicyService.getPolicy()).thenReturn(policy);
        when(authSessionRepository.countByUser_IdAndRevokedAtIsNullAndExpiresAtAfter(userId, Instant.now(clock)))
            .thenReturn(1L);
        when(authSessionRepository.findByUser_IdAndRevokedAtIsNullAndExpiresAtAfterOrderByCreatedAtAsc(userId, Instant.now(clock)))
            .thenReturn(List.of(oldest));

        sessionService.enforceLoginPolicy(user);

        ArgumentCaptor<AuthSession> captor = ArgumentCaptor.forClass(AuthSession.class);
        verify(authSessionRepository).save(captor.capture());
        assertEquals(Instant.now(clock), captor.getValue().getRevokedAt());
        assertEquals("Revoked due to concurrent session policy", captor.getValue().getRevokeReason());
    }

    @Test
    void enforceLoginPolicyRevokesEnoughOldSessionsWhenCurrentCountAlreadyExceedsLimit() {
        UUID userId = UUID.randomUUID();
        AuthUser user = new AuthUser();
        user.setPrimaryRole(UserRole.BUYER);
        setUserId(user, userId);

        AuthSession oldest = new AuthSession();
        AuthSession nextOldest = new AuthSession();
        AuthPolicySettings policy = new AuthPolicySettings();
        policy.setMaxConcurrentSessions(1);
        policy.setConcurrentSessionPolicy(AuthProperties.ConcurrentSessionPolicy.REVOKE_OLDEST);
        when(authPolicyService.getPolicy()).thenReturn(policy);
        when(authSessionRepository.countByUser_IdAndRevokedAtIsNullAndExpiresAtAfter(userId, Instant.now(clock)))
            .thenReturn(2L);
        when(authSessionRepository.findByUser_IdAndRevokedAtIsNullAndExpiresAtAfterOrderByCreatedAtAsc(userId, Instant.now(clock)))
            .thenReturn(List.of(oldest, nextOldest));

        sessionService.enforceLoginPolicy(user);

        ArgumentCaptor<AuthSession> captor = ArgumentCaptor.forClass(AuthSession.class);
        verify(authSessionRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertEquals(List.of(oldest, nextOldest), captor.getAllValues());
    }

    @Test
    void enforceLoginPolicyAllowsLoginWhenBelowLimit() {
        UUID userId = UUID.randomUUID();
        AuthUser user = user(userId);
        AuthPolicySettings policy = policy(2, AuthProperties.ConcurrentSessionPolicy.REJECT_NEW);
        when(authPolicyService.getPolicy()).thenReturn(policy);
        when(authSessionRepository.countByUser_IdAndRevokedAtIsNullAndExpiresAtAfter(userId, Instant.now(clock)))
            .thenReturn(1L);

        sessionService.enforceLoginPolicy(user);

        verify(authSessionRepository, never())
            .findByUser_IdAndRevokedAtIsNullAndExpiresAtAfterOrderByCreatedAtAsc(any(), any());
    }

    @Test
    void enforceLoginPolicyRejectsNewSessionWhenConfigured() {
        UUID userId = UUID.randomUUID();
        AuthUser user = user(userId);
        AuthPolicySettings policy = policy(1, AuthProperties.ConcurrentSessionPolicy.REJECT_NEW);
        when(authPolicyService.getPolicy()).thenReturn(policy);
        when(authSessionRepository.countByUser_IdAndRevokedAtIsNullAndExpiresAtAfter(userId, Instant.now(clock)))
            .thenReturn(1L);

        assertThrows(SessionLimitExceededException.class, () -> sessionService.enforceLoginPolicy(user));
    }

    @Test
    void enforceLoginPolicyRejectsWhenNotEnoughSessionsCanBeRevoked() {
        UUID userId = UUID.randomUUID();
        AuthUser user = user(userId);
        AuthPolicySettings policy = policy(1, AuthProperties.ConcurrentSessionPolicy.REVOKE_OLDEST);
        when(authPolicyService.getPolicy()).thenReturn(policy);
        when(authSessionRepository.countByUser_IdAndRevokedAtIsNullAndExpiresAtAfter(userId, Instant.now(clock)))
            .thenReturn(3L);
        when(authSessionRepository.findByUser_IdAndRevokedAtIsNullAndExpiresAtAfterOrderByCreatedAtAsc(userId, Instant.now(clock)))
            .thenReturn(List.of(new AuthSession()));

        assertThrows(SessionLimitExceededException.class, () -> sessionService.enforceLoginPolicy(user));
    }

    @Test
    void createSessionPersistsNewSessionDetails() {
        AuthUser user = user(UUID.randomUUID());
        Instant expiresAt = Instant.parse("2026-03-07T12:00:00Z");
        when(authSessionRepository.save(any(AuthSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthSession session = sessionService.createSession(user, "127.0.0.1", "JUnit", expiresAt, true);

        assertSame(user, session.getUser());
        assertEquals("127.0.0.1", session.getIpAddress());
        assertEquals("JUnit", session.getUserAgent());
        assertEquals(expiresAt, session.getExpiresAt());
        assertTrue(session.isTwoFactorVerified());
    }

    @Test
    void findActiveByIdReturnsSessionWhenNotExpired() {
        UUID sessionId = UUID.randomUUID();
        AuthSession session = session(user(UUID.randomUUID()));
        session.setExpiresAt(Instant.now(clock).plusSeconds(60));
        when(authSessionRepository.findActiveByIdWithUser(sessionId)).thenReturn(Optional.of(session));

        assertEquals(Optional.of(session), sessionService.findActiveById(sessionId));
    }

    @Test
    void findActiveByIdFiltersExpiredSession() {
        UUID sessionId = UUID.randomUUID();
        AuthSession session = session(user(UUID.randomUUID()));
        session.setExpiresAt(Instant.now(clock));
        when(authSessionRepository.findActiveByIdWithUser(sessionId)).thenReturn(Optional.of(session));

        assertEquals(Optional.empty(), sessionService.findActiveById(sessionId));
    }

    @Test
    void listActiveSessionsDelegatesWithCurrentTime() {
        UUID userId = UUID.randomUUID();
        List<AuthSession> sessions = List.of(new AuthSession(), new AuthSession());
        when(authSessionRepository.findByUser_IdAndRevokedAtIsNullAndExpiresAtAfterOrderByCreatedAtAsc(userId, Instant.now(clock)))
            .thenReturn(sessions);

        assertSame(sessions, sessionService.listActiveSessions(userId));
    }

    @Test
    void revokeOwnSessionRevokesOwnedSession() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        AuthSession session = session(user(userId));
        when(authSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        sessionService.revokeOwnSession(userId, sessionId);

        assertEquals(Instant.now(clock), session.getRevokedAt());
        assertEquals("Revoked by user", session.getRevokeReason());
        verify(authSessionRepository).save(session);
    }

    @Test
    void revokeOwnSessionRejectsSessionOwnedByAnotherUser() {
        UUID sessionId = UUID.randomUUID();
        AuthSession session = session(user(UUID.randomUUID()));
        when(authSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(ResourceNotFoundException.class, () -> sessionService.revokeOwnSession(UUID.randomUUID(), sessionId));

        verify(authSessionRepository, never()).save(any(AuthSession.class));
    }

    @Test
    void revokeAllSessionsForUserDelegatesWithCurrentTime() {
        UUID userId = UUID.randomUUID();

        sessionService.revokeAllSessionsForUser(userId, "manual");

        verify(authSessionRepository).revokeAllActiveByUserId(userId, "manual", Instant.now(clock));
    }

    @Test
    void markTwoFactorVerifiedUpdatesOwnedSession() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        AuthSession session = session(user(userId));
        when(authSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        sessionService.markTwoFactorVerified(userId, sessionId, true);

        assertTrue(session.isTwoFactorVerified());
        verify(authSessionRepository).save(session);
    }

    @Test
    void markTwoFactorVerifiedRejectsSessionOwnedByAnotherUser() {
        UUID sessionId = UUID.randomUUID();
        AuthSession session = session(user(UUID.randomUUID()));
        when(authSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(
            ResourceNotFoundException.class,
            () -> sessionService.markTwoFactorVerified(UUID.randomUUID(), sessionId, true)
        );

        assertFalse(session.isTwoFactorVerified());
        verify(authSessionRepository, never()).save(any(AuthSession.class));
    }

    private AuthUser user(UUID id) {
        AuthUser user = new AuthUser();
        user.setPrimaryRole(UserRole.BUYER);
        user.setStatus(UserStatus.ACTIVE);
        setUserId(user, id);
        return user;
    }

    private AuthSession session(AuthUser user) {
        AuthSession session = new AuthSession();
        session.setUser(user);
        session.setExpiresAt(Instant.now(clock).plusSeconds(300));
        return session;
    }

    private AuthPolicySettings policy(
        int maxConcurrentSessions,
        AuthProperties.ConcurrentSessionPolicy concurrentSessionPolicy
    ) {
        AuthPolicySettings policy = new AuthPolicySettings();
        policy.setMaxConcurrentSessions(maxConcurrentSessions);
        policy.setConcurrentSessionPolicy(concurrentSessionPolicy);
        return policy;
    }

    private void setUserId(AuthUser user, UUID id) {
        try {
            var field = AuthUser.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
