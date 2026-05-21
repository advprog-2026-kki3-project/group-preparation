package id.ac.ui.cs.advprog.bidmart.auth.controller;

import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.ErrorResponse;
import id.ac.ui.cs.advprog.bidmart.auth.exception.EmailAlreadyUsedException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.ForbiddenPermissionException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.InvalidCredentialsException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.LoginAttemptLimitExceededException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.PasswordPolicyViolationException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.ResourceNotFoundException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.SessionLimitExceededException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.UserDisabledException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthExceptionHandlerTest {
    private static final Instant NOW = Instant.parse("2026-05-21T10:00:00Z");

    private AuthExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new AuthExceptionHandler(Clock.fixed(NOW, ZoneOffset.UTC));
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/auth/test");
    }

    @Test
    void handleConflictBuildsConflictResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleConflict(new EmailAlreadyUsedException(), request);

        assertError(response, HttpStatus.CONFLICT, "Email is already registered.");
    }

    @Test
    void handleUnauthorizedBuildsUnauthorizedResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(new InvalidCredentialsException(), request);

        assertError(response, HttpStatus.UNAUTHORIZED, "Invalid email or password.");
    }

    @Test
    void handleForbiddenBuildsForbiddenResponse() {
        ResponseEntity<ErrorResponse> disabled = handler.handleForbidden(new UserDisabledException(), request);
        ResponseEntity<ErrorResponse> forbidden = handler.handleForbidden(new ForbiddenPermissionException(), request);

        assertError(disabled, HttpStatus.FORBIDDEN, "User account is disabled.");
        assertError(forbidden, HttpStatus.FORBIDDEN, "The authenticated user is not allowed to perform this operation.");
    }

    @Test
    void handleTooManyRequestsBuildsTooManyRequestsResponse() {
        ResponseEntity<ErrorResponse> sessionLimit = handler.handleTooManyRequests(
            new SessionLimitExceededException(),
            request
        );
        ResponseEntity<ErrorResponse> loginLimit = handler.handleTooManyRequests(
            new LoginAttemptLimitExceededException(61),
            request
        );

        assertError(sessionLimit, HttpStatus.TOO_MANY_REQUESTS, "Maximum concurrent sessions exceeded.");
        assertError(loginLimit, HttpStatus.TOO_MANY_REQUESTS, "Maximum login attempts exceeded. Try again in 1 minute 1 second.");
    }

    @Test
    void handleNotFoundBuildsNotFoundResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(new ResourceNotFoundException("Missing."), request);

        assertError(response, HttpStatus.NOT_FOUND, "Missing.");
    }

    @Test
    void handleBadRequestBuildsBadRequestResponse() {
        ResponseEntity<ErrorResponse> policy = handler.handleBadRequest(
            new PasswordPolicyViolationException("Weak password."),
            request
        );
        ResponseEntity<ErrorResponse> illegalArgument = handler.handleBadRequest(
            new IllegalArgumentException("Invalid input."),
            request
        );

        assertError(policy, HttpStatus.BAD_REQUEST, "Weak password.");
        assertError(illegalArgument, HttpStatus.BAD_REQUEST, "Invalid input.");
    }

    @Test
    void handleValidationJoinsFieldErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
            new FieldError("request", "email", "Email is required."),
            new FieldError("request", "password", "Password is required.")
        ));

        ResponseEntity<ErrorResponse> response = handler.handleValidation(exception, request);

        assertError(response, HttpStatus.BAD_REQUEST, "Email is required.; Password is required.");
    }

    private void assertError(ResponseEntity<ErrorResponse> response, HttpStatus status, String message) {
        assertEquals(status, response.getStatusCode());
        assertEquals(NOW, response.getBody().timestamp());
        assertEquals(status.value(), response.getBody().status());
        assertEquals(status.getReasonPhrase(), response.getBody().error());
        assertEquals(message, response.getBody().message());
        assertEquals("/auth/test", response.getBody().path());
    }
}
