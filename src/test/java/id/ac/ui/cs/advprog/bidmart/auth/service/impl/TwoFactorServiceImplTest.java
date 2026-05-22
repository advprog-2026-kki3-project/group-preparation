package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import id.ac.ui.cs.advprog.bidmart.auth.exception.InvalidTwoFactorCodeException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.ResourceNotFoundException;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPolicySettings;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallenge;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallengePurpose;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorMethod;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserStatus;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserTwoFactorSettings;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthUserRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.TwoFactorChallengeRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.UserTwoFactorSettingsRepository;
import id.ac.ui.cs.advprog.bidmart.auth.service.AuthPolicyService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TokenService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TotpService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TwoFactorChallengeAttemptService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TwoFactorCodeSender;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TwoFactorServiceImplTest {
    private static final Instant NOW = Instant.parse("2026-05-21T10:00:00Z");
    private static final Duration OTP_TTL = Duration.ofMinutes(5);

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private UserTwoFactorSettingsRepository settingsRepository;

    @Mock
    private TwoFactorChallengeRepository challengeRepository;

    @Mock
    private TwoFactorChallengeAttemptService challengeAttemptService;

    @Mock
    private TwoFactorCodeSender codeSender;

    @Mock
    private TokenService tokenService;

    @Mock
    private TotpService totpService;

    @Mock
    private AuthPolicyService authPolicyService;

    private TwoFactorServiceImpl twoFactorService;
    private AuthUser user;

    @BeforeEach
    void setUp() {
        twoFactorService = new TwoFactorServiceImpl(
            authUserRepository,
            settingsRepository,
            challengeRepository,
            challengeAttemptService,
            codeSender,
            tokenService,
            totpService,
            authPolicyService,
            Clock.fixed(NOW, ZoneOffset.UTC),
            new SimpleMeterRegistry()
        );
        user = user(UUID.randomUUID());
    }

    @Test
    void getOrCreateSettingsReturnsExistingSettings() {
        UserTwoFactorSettings existing = settings(user, true, TwoFactorMethod.EMAIL_OTP);
        when(settingsRepository.findById(user.getId())).thenReturn(Optional.of(existing));

        UserTwoFactorSettings result = twoFactorService.getOrCreateSettings(user);

        assertSame(existing, result);
        verify(settingsRepository, never()).save(any(UserTwoFactorSettings.class));
    }

    @Test
    void getOrCreateSettingsCreatesDisabledSettingsWhenMissing() {
        when(settingsRepository.findById(user.getId())).thenReturn(Optional.empty());
        when(settingsRepository.save(any(UserTwoFactorSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserTwoFactorSettings result = twoFactorService.getOrCreateSettings(user);

        assertSame(user, result.getUser());
        assertFalse(result.isEnabled());
    }

    @Test
    void findSettingsDelegatesToRepository() {
        UserTwoFactorSettings existing = settings(user, false, null);
        when(settingsRepository.findById(user.getId())).thenReturn(Optional.of(existing));

        assertEquals(Optional.of(existing), twoFactorService.findSettings(user.getId()));
    }

    @Test
    void createEmailOtpChallengeHashesAndSendsGeneratedCode() {
        stubPolicy();
        when(tokenService.hashRefreshToken(anyString())).thenAnswer(invocation -> "hash:" + invocation.getArgument(0));
        when(challengeRepository.save(any(TwoFactorChallenge.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<TwoFactorChallenge> challengeCaptor = ArgumentCaptor.forClass(TwoFactorChallenge.class);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        TwoFactorChallenge challenge = twoFactorService.createChallenge(
            user,
            TwoFactorChallengePurpose.ENABLE,
            TwoFactorMethod.EMAIL_OTP
        );

        verify(challengeRepository).save(challengeCaptor.capture());
        verify(codeSender).send(
            eq(user),
            eq(TwoFactorMethod.EMAIL_OTP),
            eq(TwoFactorChallengePurpose.ENABLE),
            codeCaptor.capture()
        );
        assertSame(challengeCaptor.getValue(), challenge);
        assertEquals(user, challenge.getUser());
        assertEquals(TwoFactorChallengePurpose.ENABLE, challenge.getPurpose());
        assertEquals(TwoFactorMethod.EMAIL_OTP, challenge.getMethod());
        assertEquals("hash:" + codeCaptor.getValue(), challenge.getCodeHash());
        assertEquals(0, challenge.getAttempts());
        assertEquals(5, challenge.getMaxAttempts());
        assertEquals(NOW.plus(OTP_TTL), challenge.getExpiresAt());
    }

    @Test
    void createTotpChallengeDoesNotSendEmailCode() {
        stubPolicy();
        when(tokenService.hashRefreshToken(anyString())).thenReturn("hashed-random");
        when(challengeRepository.save(any(TwoFactorChallenge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TwoFactorChallenge challenge = twoFactorService.createChallenge(
            user,
            TwoFactorChallengePurpose.CHANGE,
            TwoFactorMethod.TOTP
        );

        assertEquals(TwoFactorMethod.TOTP, challenge.getMethod());
        assertEquals("hashed-random", challenge.getCodeHash());
        verifyNoInteractions(codeSender);
    }

    @Test
    void consumeChallengeRejectsMissingChallenge() {
        UUID challengeId = UUID.randomUUID();
        when(challengeRepository.findByIdAndConsumedAtIsNull(challengeId)).thenReturn(Optional.empty());

        assertThrows(
            InvalidTwoFactorCodeException.class,
            () -> twoFactorService.consumeChallenge(challengeId, "123456", TwoFactorChallengePurpose.LOGIN)
        );
    }

    @Test
    void consumeChallengeRejectsUnexpectedPurpose() {
        UUID challengeId = UUID.randomUUID();
        TwoFactorChallenge challenge = challenge(challengeId, user, TwoFactorChallengePurpose.ENABLE, TwoFactorMethod.EMAIL_OTP);
        when(challengeRepository.findByIdAndConsumedAtIsNull(challengeId)).thenReturn(Optional.of(challenge));

        assertThrows(
            InvalidTwoFactorCodeException.class,
            () -> twoFactorService.consumeChallenge(challengeId, "123456", TwoFactorChallengePurpose.LOGIN)
        );

        verifyNoInteractions(challengeAttemptService);
    }

    @Test
    void consumeChallengeRejectsExpiredChallenge() {
        UUID challengeId = UUID.randomUUID();
        TwoFactorChallenge challenge = challenge(challengeId, user, TwoFactorChallengePurpose.LOGIN, TwoFactorMethod.EMAIL_OTP);
        challenge.setExpiresAt(NOW);
        when(challengeRepository.findByIdAndConsumedAtIsNull(challengeId)).thenReturn(Optional.of(challenge));

        assertThrows(
            InvalidTwoFactorCodeException.class,
            () -> twoFactorService.consumeChallenge(challengeId, "123456", TwoFactorChallengePurpose.LOGIN)
        );

        verifyNoInteractions(challengeAttemptService);
    }

    @Test
    void consumeTotpEnableChallengeUsesPendingSecret() {
        UUID challengeId = UUID.randomUUID();
        TwoFactorChallenge challenge = challenge(challengeId, user, TwoFactorChallengePurpose.ENABLE, TwoFactorMethod.TOTP);
        UserTwoFactorSettings settings = settings(user, false, null);
        settings.setPendingTotpSecret("pending-secret");
        when(challengeRepository.findByIdAndConsumedAtIsNull(challengeId)).thenReturn(Optional.of(challenge));
        when(settingsRepository.findById(user.getId())).thenReturn(Optional.of(settings));
        when(totpService.verify("pending-secret", "123456", NOW)).thenReturn(true);
        when(challengeRepository.save(challenge)).thenReturn(challenge);
        stubPolicyAndAttempts(challenge, 0);

        TwoFactorChallenge consumed = twoFactorService.consumeChallenge(
            challengeId,
            "123456",
            TwoFactorChallengePurpose.ENABLE
        );

        assertEquals(NOW, consumed.getConsumedAt());
    }

    @Test
    void consumeTotpDisableChallengeUsesActiveSecret() {
        UUID challengeId = UUID.randomUUID();
        TwoFactorChallenge challenge = challenge(challengeId, user, TwoFactorChallengePurpose.DISABLE, TwoFactorMethod.TOTP);
        UserTwoFactorSettings settings = settings(user, true, TwoFactorMethod.TOTP);
        settings.setTotpSecret("active-secret");
        when(challengeRepository.findByIdAndConsumedAtIsNull(challengeId)).thenReturn(Optional.of(challenge));
        when(settingsRepository.findById(user.getId())).thenReturn(Optional.of(settings));
        when(totpService.verify("active-secret", "654321", NOW)).thenReturn(true);
        when(challengeRepository.save(challenge)).thenReturn(challenge);
        stubPolicyAndAttempts(challenge, 0);

        TwoFactorChallenge consumed = twoFactorService.consumeChallenge(
            challengeId,
            "654321",
            TwoFactorChallengePurpose.DISABLE
        );

        assertEquals(NOW, consumed.getConsumedAt());
    }

    @Test
    void beginEnableStoresTotpPendingSecretAndCreatesEnableChallenge() {
        when(authUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(settingsRepository.findById(user.getId())).thenReturn(Optional.of(settings(user, false, null)));
        when(totpService.generateSecret()).thenReturn("new-secret");
        stubChallengeCreation();

        twoFactorService.beginEnable(user.getId(), TwoFactorMethod.TOTP);

        ArgumentCaptor<UserTwoFactorSettings> settingsCaptor = ArgumentCaptor.forClass(UserTwoFactorSettings.class);
        verify(settingsRepository).save(settingsCaptor.capture());
        assertEquals(TwoFactorMethod.TOTP, settingsCaptor.getValue().getPendingMethod());
        assertEquals("new-secret", settingsCaptor.getValue().getPendingTotpSecret());
    }

    @Test
    void beginEnableRejectsMissingUser() {
        UUID userId = UUID.randomUUID();
        when(authUserRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> twoFactorService.beginEnable(userId, TwoFactorMethod.EMAIL_OTP));

        verifyNoInteractions(settingsRepository, challengeRepository);
    }

    @Test
    void confirmEnablePromotesPendingTotpSecret() {
        UUID challengeId = UUID.randomUUID();
        TwoFactorChallenge challenge = challenge(challengeId, user, TwoFactorChallengePurpose.ENABLE, TwoFactorMethod.TOTP);
        UserTwoFactorSettings settings = settings(user, false, null);
        settings.setPendingMethod(TwoFactorMethod.TOTP);
        settings.setPendingTotpSecret("pending-secret");
        stubSuccessfulTotpConsume(challenge, settings, "pending-secret", TwoFactorChallengePurpose.ENABLE);
        when(settingsRepository.save(settings)).thenReturn(settings);

        UserTwoFactorSettings result = twoFactorService.confirmEnable(challengeId, "123456");

        assertTrue(result.isEnabled());
        assertEquals(TwoFactorMethod.TOTP, result.getMethod());
        assertEquals("pending-secret", result.getTotpSecret());
        assertNull(result.getPendingMethod());
        assertNull(result.getPendingTotpSecret());
    }

    @Test
    void confirmEnableFallsBackToChallengeMethodWhenNoPendingMethod() {
        UUID challengeId = UUID.randomUUID();
        TwoFactorChallenge challenge = challenge(challengeId, user, TwoFactorChallengePurpose.ENABLE, TwoFactorMethod.EMAIL_OTP);
        challenge.setCodeHash("hashed-code");
        UserTwoFactorSettings settings = settings(user, false, null);
        when(challengeRepository.findByIdAndConsumedAtIsNull(challengeId)).thenReturn(Optional.of(challenge));
        when(settingsRepository.findById(user.getId())).thenReturn(Optional.of(settings));
        when(tokenService.hashRefreshToken("123456")).thenReturn("hashed-code");
        when(challengeRepository.save(challenge)).thenReturn(challenge);
        when(settingsRepository.save(settings)).thenReturn(settings);
        stubPolicyAndAttempts(challenge, 0);

        UserTwoFactorSettings result = twoFactorService.confirmEnable(challengeId, "123456");

        assertTrue(result.isEnabled());
        assertEquals(TwoFactorMethod.EMAIL_OTP, result.getMethod());
        assertNull(result.getTotpSecret());
    }

    @Test
    void beginChangeStoresTotpPendingSecretAndCreatesChangeChallenge() {
        when(authUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(settingsRepository.findById(user.getId())).thenReturn(Optional.of(settings(user, true, TwoFactorMethod.EMAIL_OTP)));
        when(totpService.generateSecret()).thenReturn("change-secret");
        stubChallengeCreation();

        TwoFactorChallenge challenge = twoFactorService.beginChange(user.getId(), TwoFactorMethod.TOTP);

        assertEquals(TwoFactorChallengePurpose.CHANGE, challenge.getPurpose());
        assertEquals(TwoFactorMethod.TOTP, challenge.getMethod());
    }

    @Test
    void confirmChangeToEmailClearsTotpSecret() {
        UUID challengeId = UUID.randomUUID();
        TwoFactorChallenge challenge = challenge(challengeId, user, TwoFactorChallengePurpose.CHANGE, TwoFactorMethod.EMAIL_OTP);
        challenge.setCodeHash("hashed-code");
        UserTwoFactorSettings settings = settings(user, true, TwoFactorMethod.TOTP);
        settings.setTotpSecret("old-secret");
        settings.setPendingMethod(TwoFactorMethod.EMAIL_OTP);
        when(challengeRepository.findByIdAndConsumedAtIsNull(challengeId)).thenReturn(Optional.of(challenge));
        when(settingsRepository.findById(user.getId())).thenReturn(Optional.of(settings));
        when(tokenService.hashRefreshToken("123456")).thenReturn("hashed-code");
        when(challengeRepository.save(challenge)).thenReturn(challenge);
        when(settingsRepository.save(settings)).thenReturn(settings);
        stubPolicyAndAttempts(challenge, 0);

        UserTwoFactorSettings result = twoFactorService.confirmChange(challengeId, "123456");

        assertTrue(result.isEnabled());
        assertEquals(TwoFactorMethod.EMAIL_OTP, result.getMethod());
        assertNull(result.getTotpSecret());
        assertNull(result.getPendingMethod());
        assertNull(result.getPendingTotpSecret());
    }

    @Test
    void confirmChangeToTotpPromotesPendingSecret() {
        UUID challengeId = UUID.randomUUID();
        TwoFactorChallenge challenge = challenge(challengeId, user, TwoFactorChallengePurpose.CHANGE, TwoFactorMethod.TOTP);
        UserTwoFactorSettings settings = settings(user, true, TwoFactorMethod.EMAIL_OTP);
        settings.setPendingMethod(TwoFactorMethod.TOTP);
        settings.setPendingTotpSecret("new-secret");
        stubSuccessfulTotpConsume(challenge, settings, "new-secret", TwoFactorChallengePurpose.CHANGE);
        when(settingsRepository.save(settings)).thenReturn(settings);

        UserTwoFactorSettings result = twoFactorService.confirmChange(challengeId, "123456");

        assertEquals(TwoFactorMethod.TOTP, result.getMethod());
        assertEquals("new-secret", result.getTotpSecret());
        assertNull(result.getPendingMethod());
        assertNull(result.getPendingTotpSecret());
    }

    @Test
    void beginDisableUsesEmailOtpWhenSettingsAreDisabled() {
        when(authUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(settingsRepository.findById(user.getId())).thenReturn(Optional.of(settings(user, false, null)));
        stubChallengeCreation();

        TwoFactorChallenge challenge = twoFactorService.beginDisable(user.getId());

        assertEquals(TwoFactorChallengePurpose.DISABLE, challenge.getPurpose());
        assertEquals(TwoFactorMethod.EMAIL_OTP, challenge.getMethod());
    }

    @Test
    void beginDisableUsesCurrentMethodWhenSettingsAreEnabled() {
        when(authUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(settingsRepository.findById(user.getId())).thenReturn(Optional.of(settings(user, true, TwoFactorMethod.TOTP)));
        stubChallengeCreation();

        TwoFactorChallenge challenge = twoFactorService.beginDisable(user.getId());

        assertEquals(TwoFactorChallengePurpose.DISABLE, challenge.getPurpose());
        assertEquals(TwoFactorMethod.TOTP, challenge.getMethod());
    }

    @Test
    void confirmDisableClearsTwoFactorSettings() {
        UUID challengeId = UUID.randomUUID();
        TwoFactorChallenge challenge = challenge(challengeId, user, TwoFactorChallengePurpose.DISABLE, TwoFactorMethod.EMAIL_OTP);
        challenge.setCodeHash("hashed-code");
        UserTwoFactorSettings settings = settings(user, true, TwoFactorMethod.EMAIL_OTP);
        settings.setPendingMethod(TwoFactorMethod.TOTP);
        settings.setTotpSecret("active-secret");
        settings.setPendingTotpSecret("pending-secret");
        when(challengeRepository.findByIdAndConsumedAtIsNull(challengeId)).thenReturn(Optional.of(challenge));
        when(settingsRepository.findById(user.getId())).thenReturn(Optional.of(settings));
        when(tokenService.hashRefreshToken("123456")).thenReturn("hashed-code");
        when(challengeRepository.save(challenge)).thenReturn(challenge);
        when(settingsRepository.save(settings)).thenReturn(settings);
        stubPolicyAndAttempts(challenge, 0);

        UserTwoFactorSettings result = twoFactorService.confirmDisable(challengeId, "123456");

        assertFalse(result.isEnabled());
        assertNull(result.getMethod());
        assertNull(result.getPendingMethod());
        assertNull(result.getTotpSecret());
        assertNull(result.getPendingTotpSecret());
    }

    private void stubChallengeCreation() {
        stubPolicy();
        when(tokenService.hashRefreshToken(anyString())).thenReturn("hashed-code");
        when(challengeRepository.save(any(TwoFactorChallenge.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void stubSuccessfulTotpConsume(
        TwoFactorChallenge challenge,
        UserTwoFactorSettings settings,
        String secret,
        TwoFactorChallengePurpose expectedPurpose
    ) {
        when(challengeRepository.findByIdAndConsumedAtIsNull(challenge.getId())).thenReturn(Optional.of(challenge));
        when(settingsRepository.findById(user.getId())).thenReturn(Optional.of(settings));
        when(totpService.verify(secret, "123456", NOW)).thenReturn(true);
        when(challengeRepository.save(challenge)).thenReturn(challenge);
        stubPolicyAndAttempts(challenge, 0);
    }

    private void stubPolicyAndAttempts(TwoFactorChallenge challenge, long attempts) {
        stubPolicy();
        when(challengeRepository.sumAttemptsByUserAndPurposeSince(
            challenge.getUser().getId(),
            challenge.getPurpose(),
            NOW.minus(OTP_TTL)
        )).thenReturn(attempts);
    }

    private void stubPolicy() {
        AuthPolicySettings policy = new AuthPolicySettings();
        policy.setConcurrentSessionPolicy(AuthProperties.ConcurrentSessionPolicy.REVOKE_OLDEST);
        policy.setMaxConcurrentSessions(3);
        policy.setLoginAttemptLimit(5);
        policy.setLoginAttemptWindow(Duration.ofMinutes(15));
        policy.setOtpAttemptLimit(5);
        policy.setOtpTtl(OTP_TTL);
        when(authPolicyService.getPolicy()).thenReturn(policy);
    }

    private AuthUser user(UUID id) {
        AuthUser authUser = new AuthUser();
        ReflectionTestUtils.setField(authUser, "id", id);
        authUser.setEmail("user@example.com");
        authUser.setPasswordHash("hash");
        authUser.setPrimaryRole(UserRole.BUYER);
        authUser.setStatus(UserStatus.ACTIVE);
        return authUser;
    }

    private UserTwoFactorSettings settings(AuthUser authUser, boolean enabled, TwoFactorMethod method) {
        UserTwoFactorSettings settings = new UserTwoFactorSettings();
        ReflectionTestUtils.setField(settings, "userId", authUser.getId());
        settings.setUser(authUser);
        settings.setEnabled(enabled);
        settings.setMethod(method);
        return settings;
    }

    private TwoFactorChallenge challenge(
        UUID id,
        AuthUser authUser,
        TwoFactorChallengePurpose purpose,
        TwoFactorMethod method
    ) {
        TwoFactorChallenge challenge = new TwoFactorChallenge();
        ReflectionTestUtils.setField(challenge, "id", id);
        ReflectionTestUtils.setField(challenge, "createdAt", NOW.minusSeconds(30));
        challenge.setUser(authUser);
        challenge.setPurpose(purpose);
        challenge.setMethod(method);
        challenge.setCodeHash("hashed-code");
        challenge.setAttempts(0);
        challenge.setMaxAttempts(5);
        challenge.setExpiresAt(NOW.plus(OTP_TTL));
        return challenge;
    }
}
