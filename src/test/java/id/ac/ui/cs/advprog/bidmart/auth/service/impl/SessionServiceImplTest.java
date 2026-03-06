package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthSession;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserRole;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {
    @Mock
    private AuthSessionRepository authSessionRepository;

    private SessionServiceImpl sessionService;
    private AuthProperties authProperties;
    private Clock clock;

    @BeforeEach
    void setUp() {
        authProperties = new AuthProperties();
        authProperties.setMaxConcurrentSessions(1);
        authProperties.setConcurrentSessionPolicy(AuthProperties.ConcurrentSessionPolicy.REVOKE_OLDEST);
        clock = Clock.fixed(Instant.parse("2026-03-06T12:00:00Z"), ZoneOffset.UTC);
        sessionService = new SessionServiceImpl(authSessionRepository, authProperties, clock);
    }

    @Test
    void enforceLoginPolicyRevokesOldestWhenLimitReached() {
        UUID userId = UUID.randomUUID();
        AuthUser user = new AuthUser();
        user.setPrimaryRole(UserRole.BUYER);
        setUserId(user, userId);

        AuthSession oldest = new AuthSession();
        when(authSessionRepository.countByUser_IdAndRevokedAtIsNull(userId)).thenReturn(1L);
        when(authSessionRepository.findFirstByUser_IdAndRevokedAtIsNullOrderByCreatedAtAsc(userId))
            .thenReturn(Optional.of(oldest));

        sessionService.enforceLoginPolicy(user);

        ArgumentCaptor<AuthSession> captor = ArgumentCaptor.forClass(AuthSession.class);
        verify(authSessionRepository).save(captor.capture());
        assertEquals(Instant.now(clock), captor.getValue().getRevokedAt());
        assertEquals("Revoked due to concurrent session policy", captor.getValue().getRevokeReason());
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
