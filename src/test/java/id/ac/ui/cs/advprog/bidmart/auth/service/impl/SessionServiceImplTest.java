package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPolicySettings;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthSession;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserRole;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
