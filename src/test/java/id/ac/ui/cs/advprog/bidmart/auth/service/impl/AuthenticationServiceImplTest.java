package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import id.ac.ui.cs.advprog.bidmart.auth.exception.EmailAlreadyUsedException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.InvalidCredentialsException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.InvalidRefreshTokenException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.UserDisabledException;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPolicySettings;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthSession;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.RefreshToken;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallenge;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallengePurpose;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorMethod;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserStatus;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserTwoFactorSettings;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthRoleRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthUserRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthUserRoleRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.RefreshTokenRepository;
import id.ac.ui.cs.advprog.bidmart.auth.service.AuthPolicyService;
import id.ac.ui.cs.advprog.bidmart.auth.service.CredentialService;
import id.ac.ui.cs.advprog.bidmart.auth.service.LoginAttemptService;
import id.ac.ui.cs.advprog.bidmart.auth.service.SessionService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TokenService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TwoFactorService;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.AuthTokens;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.LoginCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.LoginResult;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.RefreshCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.RegisterCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.TokenPair;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.TwoFactorVerifyCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {
    private static final Instant NOW = Instant.parse("2026-05-21T10:00:00Z");

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuthRoleRepository authRoleRepository;

    @Mock
    private AuthUserRoleRepository authUserRoleRepository;

    @Mock
    private AuthPolicyService authPolicyService;

    @Mock
    private CredentialService credentialService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private SessionService sessionService;

    @Mock
    private TokenService tokenService;

    @Mock
    private TwoFactorService twoFactorService;

    private AuthProperties authProperties;
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        authProperties = new AuthProperties();
        authProperties.setRefreshTokenTtl(Duration.ofDays(7));
        authProperties.setRefreshRotationEnabled(true);
        authProperties.setRefreshReuseDetectionEnabled(true);
        authenticationService = new AuthenticationServiceImpl(
            authUserRepository,
            refreshTokenRepository,
            authRoleRepository,
            authUserRoleRepository,
            authPolicyService,
            credentialService,
            loginAttemptService,
            sessionService,
            tokenService,
            twoFactorService,
            authProperties,
            Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(authUserRepository.existsByEmailIgnoreCase("buyer@example.com")).thenReturn(true);

        assertThrows(
            EmailAlreadyUsedException.class,
            () -> authenticationService.register(new RegisterCommand(" Buyer@Example.com ", "secret", UserRole.BUYER))
        );

        verifyNoInteractions(credentialService, authRoleRepository, authUserRoleRepository);
    }

    @Test
    void registerDemotesAdministratorWhenUsersAlreadyExist() {
        AuthRole buyerRole = role(UUID.randomUUID(), "BUYER");
        when(authUserRepository.existsByEmailIgnoreCase("admin@example.com")).thenReturn(false);
        when(authUserRepository.count()).thenReturn(1L);
        when(credentialService.hashPassword("secret")).thenReturn("hashed");
        when(authUserRepository.save(any(AuthUser.class))).thenAnswer(invocation -> {
            AuthUser user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            return user;
        });
        when(authRoleRepository.findByNameIgnoreCase("BUYER")).thenReturn(Optional.of(buyerRole));

        authenticationService.register(new RegisterCommand("admin@example.com", "secret", UserRole.ADMINISTRATOR));

        verify(authRoleRepository).findByNameIgnoreCase("BUYER");
        verify(authUserRoleRepository).save(any());
    }

    @Test
    void loginRecordsFailureWhenUserIsMissing() {
        LoginCommand command = loginCommand();
        stubLoginPolicy(0);
        when(authUserRepository.findByEmailIgnoreCase("buyer@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(command));

        verify(loginAttemptService).recordAttempt("buyer@example.com", "127.0.0.1", false);
    }

    @Test
    void loginRejectsDisabledUser() {
        LoginCommand command = loginCommand();
        AuthUser user = user(UUID.randomUUID(), UserStatus.DISABLED);
        stubLoginPolicy(0);
        when(authUserRepository.findByEmailIgnoreCase("buyer@example.com")).thenReturn(Optional.of(user));

        assertThrows(UserDisabledException.class, () -> authenticationService.login(command));

        verify(loginAttemptService).recordAttempt("buyer@example.com", "127.0.0.1", false);
    }

    @Test
    void loginReturnsTwoFactorChallengeWhenEnabled() {
        LoginCommand command = loginCommand();
        AuthUser user = user(UUID.randomUUID(), UserStatus.ACTIVE);
        UserTwoFactorSettings settings = settings(true, TwoFactorMethod.EMAIL_OTP);
        TwoFactorChallenge challenge = challenge(UUID.randomUUID(), user, TwoFactorMethod.EMAIL_OTP);
        stubLoginPolicy(0);
        when(authUserRepository.findByEmailIgnoreCase("buyer@example.com")).thenReturn(Optional.of(user));
        when(credentialService.matches("secret", "hash")).thenReturn(true);
        when(twoFactorService.getOrCreateSettings(user)).thenReturn(settings);
        when(twoFactorService.createChallenge(user, TwoFactorChallengePurpose.LOGIN, TwoFactorMethod.EMAIL_OTP))
            .thenReturn(challenge);

        LoginResult result = authenticationService.login(command);

        assertTrue(result.requiresTwoFactor());
        assertEquals(challenge.getId(), result.challengeId());
        verify(sessionService, never()).createSession(any(), any(), any(), any(), eq(false));
    }

    @Test
    void verifyTwoFactorLoginIssuesTokens() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        AuthUser user = user(userId, UserStatus.ACTIVE);
        TwoFactorChallenge challenge = challenge(UUID.randomUUID(), user, TwoFactorMethod.EMAIL_OTP);
        AuthSession session = session(sessionId, user);
        TokenPair tokenPair = tokenPair(UUID.randomUUID());
        when(twoFactorService.consumeChallenge(challenge.getId(), "123456", TwoFactorChallengePurpose.LOGIN))
            .thenReturn(challenge);
        when(sessionService.createSession(user, "127.0.0.1", "JUnit", NOW.plus(Duration.ofDays(7)), true))
            .thenReturn(session);
        when(tokenService.issueTokenPair(userId, sessionId, NOW, null)).thenReturn(tokenPair);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthTokens tokens = authenticationService.verifyTwoFactorLogin(
            new TwoFactorVerifyCommand(challenge.getId(), "123456", "127.0.0.1", "JUnit")
        );

        assertEquals("access", tokens.accessToken());
        assertEquals("refresh", tokens.refreshToken());
        verify(sessionService).enforceLoginPolicy(user);
    }

    @Test
    void verifyTwoFactorLoginRejectsDisabledUser() {
        AuthUser user = user(UUID.randomUUID(), UserStatus.DISABLED);
        TwoFactorChallenge challenge = challenge(UUID.randomUUID(), user, TwoFactorMethod.EMAIL_OTP);
        when(twoFactorService.consumeChallenge(challenge.getId(), "123456", TwoFactorChallengePurpose.LOGIN))
            .thenReturn(challenge);

        assertThrows(
            UserDisabledException.class,
            () -> authenticationService.verifyTwoFactorLogin(
                new TwoFactorVerifyCommand(challenge.getId(), "123456", "127.0.0.1", "JUnit")
            )
        );

        verifyNoInteractions(sessionService);
    }

    @Test
    void refreshRejectsRevokedTokenAndRevokesFamilyOnReuseDetection() {
        UUID familyId = UUID.randomUUID();
        AuthUser user = user(UUID.randomUUID(), UserStatus.ACTIVE);
        AuthSession session = session(UUID.randomUUID(), user);
        RefreshToken existing = refreshToken(session, familyId, NOW.plusSeconds(60));
        existing.setRevokedAt(NOW.minusSeconds(5));
        RefreshToken familyToken = refreshToken(session, familyId, NOW.plusSeconds(60));
        when(tokenService.hashRefreshToken("refresh")).thenReturn("refresh-hash");
        when(refreshTokenRepository.findByTokenHash("refresh-hash")).thenReturn(Optional.of(existing));
        when(refreshTokenRepository.findByTokenFamilyIdAndRevokedAtIsNull(familyId)).thenReturn(List.of(familyToken));

        assertThrows(InvalidRefreshTokenException.class, () -> authenticationService.refresh(new RefreshCommand("refresh")));

        assertEquals(NOW, familyToken.getRevokedAt());
        verify(refreshTokenRepository).saveAll(List.of(familyToken));
        verify(sessionService).revokeSession(session, "Revoked due to refresh token replay detection", NOW);
    }

    @Test
    void refreshRejectsExpiredSession() {
        AuthUser user = user(UUID.randomUUID(), UserStatus.ACTIVE);
        AuthSession session = session(UUID.randomUUID(), user);
        session.setExpiresAt(NOW.minusSeconds(1));
        RefreshToken existing = refreshToken(session, UUID.randomUUID(), NOW.plusSeconds(60));
        when(tokenService.hashRefreshToken("refresh")).thenReturn("refresh-hash");
        when(refreshTokenRepository.findByTokenHash("refresh-hash")).thenReturn(Optional.of(existing));

        assertThrows(InvalidRefreshTokenException.class, () -> authenticationService.refresh(new RefreshCommand("refresh")));
    }

    @Test
    void refreshRejectsDisabledUser() {
        AuthUser user = user(UUID.randomUUID(), UserStatus.DISABLED);
        AuthSession session = session(UUID.randomUUID(), user);
        RefreshToken existing = refreshToken(session, UUID.randomUUID(), NOW.plusSeconds(60));
        when(tokenService.hashRefreshToken("refresh")).thenReturn("refresh-hash");
        when(refreshTokenRepository.findByTokenHash("refresh-hash")).thenReturn(Optional.of(existing));

        assertThrows(UserDisabledException.class, () -> authenticationService.refresh(new RefreshCommand("refresh")));
    }

    @Test
    void refreshCanIssueReplacementWithoutRotation() {
        authProperties.setRefreshRotationEnabled(false);
        UUID familyId = UUID.randomUUID();
        AuthUser user = user(UUID.randomUUID(), UserStatus.ACTIVE);
        AuthSession session = session(UUID.randomUUID(), user);
        RefreshToken existing = refreshToken(session, familyId, NOW.plusSeconds(60));
        TokenPair tokenPair = tokenPair(familyId);
        when(tokenService.hashRefreshToken("refresh")).thenReturn("refresh-hash");
        when(refreshTokenRepository.findByTokenHash("refresh-hash")).thenReturn(Optional.of(existing));
        when(tokenService.issueTokenPair(user.getId(), session.getId(), NOW, familyId)).thenReturn(tokenPair);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthTokens tokens = authenticationService.refresh(new RefreshCommand("refresh"));

        assertEquals("access", tokens.accessToken());
        assertEquals("refresh", tokens.refreshToken());
        assertEquals(null, existing.getRevokedAt());
    }

    @Test
    void registerRejectsBlankInputBeforeRepositoryAccess() {
        assertThrows(
            IllegalArgumentException.class,
            () -> authenticationService.register(new RegisterCommand(" ", "secret", UserRole.BUYER))
        );

        verifyNoInteractions(authUserRepository);
    }

    private void stubLoginPolicy(long failedAttempts) {
        AuthPolicySettings policy = new AuthPolicySettings();
        policy.setLoginAttemptLimit(5);
        policy.setLoginAttemptWindow(Duration.ofMinutes(15));
        when(authPolicyService.getPolicy()).thenReturn(policy);
        when(loginAttemptService.countFailedAttemptsSince("buyer@example.com", NOW.minus(Duration.ofMinutes(15))))
            .thenReturn(failedAttempts);
    }

    private LoginCommand loginCommand() {
        return new LoginCommand(" Buyer@Example.com ", "secret", "127.0.0.1", "JUnit");
    }

    private AuthUser user(UUID id, UserStatus status) {
        AuthUser user = new AuthUser();
        ReflectionTestUtils.setField(user, "id", id);
        user.setEmail("buyer@example.com");
        user.setPasswordHash("hash");
        user.setPrimaryRole(UserRole.BUYER);
        user.setStatus(status);
        return user;
    }

    private AuthRole role(UUID id, String name) {
        AuthRole role = new AuthRole();
        ReflectionTestUtils.setField(role, "id", id);
        role.setName(name);
        return role;
    }

    private UserTwoFactorSettings settings(boolean enabled, TwoFactorMethod method) {
        UserTwoFactorSettings settings = new UserTwoFactorSettings();
        settings.setEnabled(enabled);
        settings.setMethod(method);
        return settings;
    }

    private TwoFactorChallenge challenge(UUID id, AuthUser user, TwoFactorMethod method) {
        TwoFactorChallenge challenge = new TwoFactorChallenge();
        ReflectionTestUtils.setField(challenge, "id", id);
        challenge.setUser(user);
        challenge.setPurpose(TwoFactorChallengePurpose.LOGIN);
        challenge.setMethod(method);
        return challenge;
    }

    private AuthSession session(UUID id, AuthUser user) {
        AuthSession session = new AuthSession();
        ReflectionTestUtils.setField(session, "id", id);
        session.setUser(user);
        session.setExpiresAt(NOW.plus(Duration.ofDays(7)));
        return session;
    }

    private RefreshToken refreshToken(AuthSession session, UUID familyId, Instant expiresAt) {
        RefreshToken token = new RefreshToken();
        ReflectionTestUtils.setField(token, "id", UUID.randomUUID());
        token.setSession(session);
        token.setTokenHash("refresh-hash");
        token.setTokenFamilyId(familyId);
        token.setExpiresAt(expiresAt);
        return token;
    }

    private TokenPair tokenPair(UUID familyId) {
        return new TokenPair(
            "access",
            NOW.plus(Duration.ofMinutes(15)),
            "refresh",
            "replacement-hash",
            NOW.plus(Duration.ofDays(7)),
            familyId
        );
    }
}
