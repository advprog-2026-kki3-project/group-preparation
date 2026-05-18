package id.ac.ui.cs.advprog.bidmart.auth.controller;

import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.BeginTwoFactorRequest;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.TwoFactorSettingsResponse;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.TwoFactorVerifyRequest;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallenge;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorMethod;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserTwoFactorSettings;
import id.ac.ui.cs.advprog.bidmart.auth.service.TotpService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TwoFactorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountSecurityControllerTest {
    private TwoFactorService twoFactorService;
    private TotpService totpService;
    private AccountSecurityController controller;
    private Authentication authentication;
    private UUID userId;

    @BeforeEach
    void setUp() {
        twoFactorService = mock(TwoFactorService.class);
        totpService = mock(TotpService.class);
        controller = new AccountSecurityController(twoFactorService, totpService);
        authentication = mock(Authentication.class);
        userId = UUID.randomUUID();
        when(authentication.getName()).thenReturn(userId.toString());
    }

    @Test
    void beginEnableUsesEnableFlow() {
        TwoFactorChallenge challenge = challenge();
        when(twoFactorService.beginEnable(userId, TwoFactorMethod.EMAIL_OTP)).thenReturn(challenge);

        controller.beginEnable(new BeginTwoFactorRequest(TwoFactorMethod.EMAIL_OTP), authentication);

        verify(twoFactorService).beginEnable(userId, TwoFactorMethod.EMAIL_OTP);
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

        TwoFactorSettingsResponse response = controller.confirmEnable(new TwoFactorVerifyRequest(challengeId, "123456"));

        assertTrue(response.enabled());
        assertEquals("EMAIL_OTP", response.method());
        verify(twoFactorService).confirmEnable(challengeId, "123456");
    }

    @Test
    void confirmChangeUsesChangeConfirmation() {
        UUID challengeId = UUID.randomUUID();
        UserTwoFactorSettings settings = enabledSettings();
        when(twoFactorService.confirmChange(challengeId, "123456")).thenReturn(settings);

        TwoFactorSettingsResponse response = controller.confirmChange(new TwoFactorVerifyRequest(challengeId, "123456"));

        assertTrue(response.enabled());
        assertEquals("EMAIL_OTP", response.method());
        verify(twoFactorService).confirmChange(challengeId, "123456");
    }

    private TwoFactorChallenge challenge() {
        TwoFactorChallenge challenge = new TwoFactorChallenge();
        challenge.setMethod(TwoFactorMethod.EMAIL_OTP);
        challenge.setExpiresAt(Instant.now().plusSeconds(300));
        return challenge;
    }

    private UserTwoFactorSettings enabledSettings() {
        UserTwoFactorSettings settings = new UserTwoFactorSettings();
        settings.setEnabled(true);
        settings.setMethod(TwoFactorMethod.EMAIL_OTP);
        return settings;
    }
}
