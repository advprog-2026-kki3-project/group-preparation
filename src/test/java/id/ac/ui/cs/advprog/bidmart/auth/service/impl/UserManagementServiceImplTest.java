package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.exception.ResourceNotFoundException;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserStatus;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthUserRepository;
import id.ac.ui.cs.advprog.bidmart.auth.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceImplTest {
    private static final Instant NOW = Instant.parse("2026-05-21T10:00:00Z");

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private SessionService sessionService;

    private UserManagementServiceImpl userManagementService;

    @BeforeEach
    void setUp() {
        userManagementService = new UserManagementServiceImpl(
            authUserRepository,
            sessionService,
            Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void listUsersDelegatesToRepository() {
        List<AuthUser> users = List.of(user(UUID.randomUUID()), user(UUID.randomUUID()));
        when(authUserRepository.findAllByOrderByEmailAsc()).thenReturn(users);

        assertSame(users, userManagementService.listUsers());
    }

    @Test
    void disableUserMarksUserDisabledAndRevokesSessions() {
        UUID userId = UUID.randomUUID();
        AuthUser user = user(userId);
        when(authUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(authUserRepository.save(user)).thenReturn(user);

        AuthUser result = userManagementService.disableUser(userId);

        assertSame(user, result);
        assertEquals(UserStatus.DISABLED, result.getStatus());
        assertEquals(NOW, result.getDisabledAt());
        verify(sessionService).revokeAllSessionsForUser(userId, "Revoked because user was disabled");
    }

    @Test
    void disableUserRejectsMissingUser() {
        UUID userId = UUID.randomUUID();
        when(authUserRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userManagementService.disableUser(userId));

        verify(authUserRepository, never()).save(org.mockito.ArgumentMatchers.any(AuthUser.class));
        verifyNoInteractions(sessionService);
    }

    private AuthUser user(UUID id) {
        AuthUser user = new AuthUser();
        ReflectionTestUtils.setField(user, "id", id);
        user.setEmail(id + "@example.com");
        user.setPasswordHash("hash");
        user.setPrimaryRole(UserRole.BUYER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
