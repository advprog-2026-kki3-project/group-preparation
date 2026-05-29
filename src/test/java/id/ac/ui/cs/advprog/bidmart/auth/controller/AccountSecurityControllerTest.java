package id.ac.ui.cs.advprog.bidmart.auth.controller;

import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.BeginTwoFactorRequest;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.TwoFactorChallengeResponse;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.TwoFactorSettingsResponse;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.TwoFactorVerifyRequest;
import id.ac.ui.cs.advprog.bidmart.auth.exception.InvalidAccessTokenException;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallenge;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorMethod;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserTwoFactorSettings;
import id.ac.ui.cs.advprog.bidmart.auth.security.AuthRequestDetails;
import id.ac.ui.cs.advprog.bidmart.auth.service.SessionService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TotpService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TwoFactorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.util.ReflectionTestUtils;

class AccountSecurityControllerTest {
    private TwoFactorService twoFactorService;
    private TotpService totpService;
    private SessionService sessionService;
    private AccountSecurityController controller;
    private Authentication authentication;
    private UUID userId;

    @BeforeEach
    void setUp() {
        twoFactorService = mock(TwoFactorService.class);
        totpService = mock(TotpService.class);
        sessionService = mock(SessionService.class);
        controller = new AccountSecurityController(twoFactorService, totpService, sessionService);
        authentication = mock(Authentication.class);
        userId = UUID.randomUUID();
        when(authentication.getName()).thenReturn(userId.toString());
        when(authentication.getDetails()).thenReturn(new AuthRequestDetails(UUID.randomUUID(), false));
    }

    @Test
    void getSettingsReturnsDisabledWhenMissing() {
        when(twoFactorService.findSettings(userId)).thenReturn(Optional.empty());

        TwoFactorSettingsResponse response = controller.getSettings(authentication);

        assertEquals(false, response.enabled());
        assertNull(response.method());
        assertNull(response.pendingMethod());
    }

    @Test
    void getSettingsMapsCurrentSettings() {
        UserTwoFactorSettings settings = enabledSettings();
        settings.setPendingMethod(TwoFactorMethod.TOTP);
        when(twoFactorService.findSettings(userId)).thenReturn(Optional.of(settings));

        TwoFactorSettingsResponse response = controller.getSettings(authentication);

        assertTrue(response.enabled());
        assertEquals("EMAIL_OTP", response.method());
        assertEquals("TOTP", response.pendingMethod());
    }

    @Test
    void beginEnableUsesEnableFlow() {
        TwoFactorChallenge challenge = challenge();
        when(twoFactorService.beginEnable(userId, TwoFactorMethod.EMAIL_OTP)).thenReturn(challenge);

        controller.beginEnable(new BeginTwoFactorRequest(TwoFactorMethod.EMAIL_OTP), authentication);

        verify(twoFactorService).beginEnable(userId, TwoFactorMethod.EMAIL_OTP);
    }

    @Test
    void beginEnableTotpIncludesPendingSecretAndUri() {
        AuthUser user = user();
        TwoFactorChallenge challenge = challenge(TwoFactorMethod.TOTP, user);
        UserTwoFactorSettings settings = new UserTwoFactorSettings();
        settings.setPendingTotpSecret("totp-secret");
        when(twoFactorService.beginEnable(userId, TwoFactorMethod.TOTP)).thenReturn(challenge);
        when(twoFactorService.findSettings(userId)).thenReturn(Optional.of(settings));
        when(totpService.buildOtpAuthUri("BidMart", "buyer@example.com", "totp-secret")).thenReturn("otpauth://uri");

        TwoFactorChallengeResponse response = controller.beginEnable(
            new BeginTwoFactorRequest(TwoFactorMethod.TOTP),
            authentication
        );

        assertEquals(challenge.getId(), response.challengeId());
        assertEquals("TOTP", response.method());
        assertEquals("totp-secret", response.totpSecret());
        assertEquals("otpauth://uri", response.totpUri());
    }

    @Test
    void beginChangeUsesChangeFlow() {
        TwoFactorChallenge challenge = challenge();
        when(twoFactorService.beginChange(userId, TwoFactorMethod.EMAIL_OTP)).thenReturn(challenge);

        controller.beginChange(new BeginTwoFactorRequest(TwoFactorMethod.EMAIL_OTP), authentication);

        verify(twoFactorService).beginChange(userId, TwoFactorMethod.EMAIL_OTP);
    }

    @Test
    void confirmEnableUsesEnableConfirmation() {
        UUID challengeId = UUID.randomUUID();
        UserTwoFactorSettings settings = enabledSettings();
        when(twoFactorService.confirmEnable(challengeId, "123456")).thenReturn(settings);

        TwoFactorSettingsResponse response = controller.confirmEnable(new TwoFactorVerifyRequest(challengeId, "123456"), authentication);

        assertTrue(response.enabled());
        assertEquals("EMAIL_OTP", response.method());
        verify(twoFactorService).confirmEnable(challengeId, "123456");
    }

    @Test
    void confirmChangeUsesChangeConfirmation() {
        UUID challengeId = UUID.randomUUID();
        UserTwoFactorSettings settings = enabledSettings();
        when(twoFactorService.confirmChange(challengeId, "123456")).thenReturn(settings);

        TwoFactorSettingsResponse response = controller.confirmChange(new TwoFactorVerifyRequest(challengeId, "123456"), authentication);

        assertTrue(response.enabled());
        assertEquals("EMAIL_OTP", response.method());
        verify(twoFactorService).confirmChange(challengeId, "123456");
    }

    @Test
    void beginDisableUsesDisableFlow() {
        TwoFactorChallenge challenge = challenge();
        when(twoFactorService.beginDisable(userId)).thenReturn(challenge);

        TwoFactorChallengeResponse response = controller.beginDisable(authentication);

        assertEquals(challenge.getId(), response.challengeId());
        assertEquals("EMAIL_OTP", response.method());
        verify(twoFactorService).beginDisable(userId);
    }

    @Test
    void confirmDisableMarksSessionUnverified() {
        UUID challengeId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        when(authentication.getDetails()).thenReturn(new AuthRequestDetails(sessionId, true));
        UserTwoFactorSettings settings = new UserTwoFactorSettings();
        settings.setEnabled(false);
        when(twoFactorService.confirmDisable(challengeId, "123456")).thenReturn(settings);

        TwoFactorSettingsResponse response = controller.confirmDisable(
            new TwoFactorVerifyRequest(challengeId, "123456"),
            authentication
        );

        assertEquals(false, response.enabled());
        verify(sessionService).markTwoFactorVerified(userId, sessionId, false);
    }

    @Test
    void confirmEnableRejectsAuthenticationWithoutSessionDetails() {
        UUID challengeId = UUID.randomUUID();
        when(authentication.getDetails()).thenReturn("not-details");
        when(twoFactorService.confirmEnable(challengeId, "123456")).thenReturn(enabledSettings());

        assertThrows(
            InvalidAccessTokenException.class,
            () -> controller.confirmEnable(new TwoFactorVerifyRequest(challengeId, "123456"), authentication)
        );
    }

    private TwoFactorChallenge challenge() {
        return challenge(TwoFactorMethod.EMAIL_OTP, user());
    }

    private TwoFactorChallenge challenge(TwoFactorMethod method, AuthUser user) {
        TwoFactorChallenge challenge = new TwoFactorChallenge();
        ReflectionTestUtils.setField(challenge, "id", UUID.randomUUID());
        challenge.setUser(user);
        challenge.setMethod(method);
        challenge.setExpiresAt(Instant.now().plusSeconds(300));
        return challenge;
    }

    private AuthUser user() {
        AuthUser user = new AuthUser();
        ReflectionTestUtils.setField(user, "id", userId);
        user.setEmail("buyer@example.com");
        return user;
    }

    private UserTwoFactorSettings enabledSettings() {
        UserTwoFactorSettings settings = new UserTwoFactorSettings();
        settings.setEnabled(true);
        settings.setMethod(TwoFactorMethod.EMAIL_OTP);
        return settings;
    }
}
