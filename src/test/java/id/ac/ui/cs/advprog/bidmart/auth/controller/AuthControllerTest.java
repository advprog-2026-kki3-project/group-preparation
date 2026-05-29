package id.ac.ui.cs.advprog.bidmart.auth.controller;

import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.AuthTokenResponse;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.LoginRequest;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.LoginResponse;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.RefreshRequest;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.RegisterRequest;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.TwoFactorVerifyRequest;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorMethod;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserRole;
import id.ac.ui.cs.advprog.bidmart.auth.service.AuthenticationService;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.AuthTokens;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.LoginCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.LoginResult;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.RefreshCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.RegisterCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.TwoFactorVerifyCommand;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {
    private static final Instant ACCESS_EXPIRES_AT = Instant.parse("2026-05-21T10:15:00Z");
    private static final Instant REFRESH_EXPIRES_AT = Instant.parse("2026-05-28T10:00:00Z");

    private AuthenticationService authenticationService;
    private AuthController controller;
    private HttpServletRequest servletRequest;

    @BeforeEach
    void setUp() {
        authenticationService = mock(AuthenticationService.class);
        controller = new AuthController(authenticationService);
        servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(servletRequest.getHeader("User-Agent")).thenReturn("JUnit");
    }

    @Test
    void registerDelegatesAndReturnsCreated() {
        ResponseEntity<Void> response = controller.register(
            new RegisterRequest("buyer@example.com", "StrongPass123!", UserRole.BUYER)
        );

        ArgumentCaptor<RegisterCommand> captor = ArgumentCaptor.forClass(RegisterCommand.class);
        verify(authenticationService).register(captor.capture());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("buyer@example.com", captor.getValue().email());
        assertEquals("StrongPass123!", captor.getValue().rawPassword());
        assertEquals(UserRole.BUYER, captor.getValue().requestedRole());
    }

    @Test
    void loginMapsAuthenticatedResult() {
        when(authenticationService.login(org.mockito.ArgumentMatchers.any(LoginCommand.class)))
            .thenReturn(LoginResult.authenticated(tokens()));

        ResponseEntity<LoginResponse> response = controller.login(
            new LoginRequest("buyer@example.com", "StrongPass123!"),
            servletRequest
        );

        ArgumentCaptor<LoginCommand> captor = ArgumentCaptor.forClass(LoginCommand.class);
        verify(authenticationService).login(captor.capture());
        assertEquals("127.0.0.1", captor.getValue().ipAddress());
        assertEquals("JUnit", captor.getValue().userAgent());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Bearer", response.getBody().tokenType());
        assertEquals("access", response.getBody().accessToken());
        assertEquals("refresh", response.getBody().refreshToken());
        assertEquals(ACCESS_EXPIRES_AT, response.getBody().accessExpiresAt());
        assertEquals(REFRESH_EXPIRES_AT, response.getBody().refreshExpiresAt());
        assertEquals(false, response.getBody().requiresTwoFactor());
    }

    @Test
    void loginMapsTwoFactorRequiredResult() {
        UUID challengeId = UUID.randomUUID();
        when(authenticationService.login(org.mockito.ArgumentMatchers.any(LoginCommand.class)))
            .thenReturn(LoginResult.twoFactorRequired(challengeId, TwoFactorMethod.TOTP));

        ResponseEntity<LoginResponse> response = controller.login(
            new LoginRequest("buyer@example.com", "StrongPass123!"),
            servletRequest
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().requiresTwoFactor());
        assertEquals(challengeId, response.getBody().challengeId());
        assertEquals("TOTP", response.getBody().twoFactorMethod());
        assertNull(response.getBody().accessToken());
        assertNull(response.getBody().refreshToken());
    }

    @Test
    void verifyTwoFactorLoginMapsTokens() {
        UUID challengeId = UUID.randomUUID();
        when(authenticationService.verifyTwoFactorLogin(org.mockito.ArgumentMatchers.any(TwoFactorVerifyCommand.class)))
            .thenReturn(tokens());

        ResponseEntity<AuthTokenResponse> response = controller.verifyTwoFactorLogin(
            new TwoFactorVerifyRequest(challengeId, "123456"),
            servletRequest
        );

        ArgumentCaptor<TwoFactorVerifyCommand> captor = ArgumentCaptor.forClass(TwoFactorVerifyCommand.class);
        verify(authenticationService).verifyTwoFactorLogin(captor.capture());
        assertEquals(challengeId, captor.getValue().challengeId());
        assertEquals("123456", captor.getValue().code());
        assertEquals("127.0.0.1", captor.getValue().ipAddress());
        assertEquals("Bearer", response.getBody().tokenType());
        assertEquals("access", response.getBody().accessToken());
    }

    @Test
    void refreshMapsTokens() {
        when(authenticationService.refresh(org.mockito.ArgumentMatchers.any(RefreshCommand.class))).thenReturn(tokens());

        ResponseEntity<AuthTokenResponse> response = controller.refresh(new RefreshRequest("refresh"));

        ArgumentCaptor<RefreshCommand> captor = ArgumentCaptor.forClass(RefreshCommand.class);
        verify(authenticationService).refresh(captor.capture());
        assertEquals("refresh", captor.getValue().refreshToken());
        assertEquals("Bearer", response.getBody().tokenType());
        assertEquals(REFRESH_EXPIRES_AT, response.getBody().refreshExpiresAt());
    }

    private AuthTokens tokens() {
        return new AuthTokens("access", "refresh", ACCESS_EXPIRES_AT, REFRESH_EXPIRES_AT);
    }
}
