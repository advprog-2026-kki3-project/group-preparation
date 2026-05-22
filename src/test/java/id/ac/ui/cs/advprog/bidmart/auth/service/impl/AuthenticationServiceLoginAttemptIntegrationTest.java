package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import id.ac.ui.cs.advprog.bidmart.auth.exception.InvalidCredentialsException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.LoginAttemptLimitExceededException;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserRole;
import id.ac.ui.cs.advprog.bidmart.auth.service.AuthPolicyService;
import id.ac.ui.cs.advprog.bidmart.auth.service.AuthenticationService;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.LoginCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.RegisterCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AuthenticationServiceLoginAttemptIntegrationTest {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthPolicyService authPolicyService;

    @Test
    void failedPasswordAttemptsBlockCorrectPasswordWithinWindow() {
        String email = "attempt-" + UUID.randomUUID() + "@example.com";
        authenticationService.register(new RegisterCommand(email, "StrongPass123!", UserRole.BUYER));
        authPolicyService.updatePolicy(
            3,
            AuthProperties.ConcurrentSessionPolicy.REVOKE_OLDEST,
            4,
            Duration.ofMinutes(15),
            5,
            Duration.ofMinutes(5)
        );

        for (int attempt = 0; attempt < 4; attempt++) {
            assertThrows(
                InvalidCredentialsException.class,
                () -> authenticationService.login(new LoginCommand(email, "WrongPass123!", "127.0.0.1", "test"))
            );
        }

        LoginAttemptLimitExceededException exception = assertThrows(
            LoginAttemptLimitExceededException.class,
            () -> authenticationService.login(new LoginCommand(email, "StrongPass123!", "127.0.0.1", "test"))
        );
        assertTrue(exception.getMessage().contains("Try again in"));
    }
}
