package id.ac.ui.cs.advprog.bidmart.auth.controller;

import id.ac.ui.cs.advprog.bidmart.auth.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SessionControllerTest {
    private SessionService sessionService;
    private SessionController controller;

    @BeforeEach
    void setUp() {
        sessionService = mock(SessionService.class);
        controller = new SessionController(sessionService);
    }

    @Test
    void revokeCurrentUsesAuthenticatedSessionId() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userId.toString(),
            null
        );
        authentication.setDetails(sessionId);

        controller.revokeCurrent(authentication);

        verify(sessionService).revokeOwnSession(userId, sessionId);
    }

    @Test
    void revokeSpecificSessionStillUsesRequestedSessionId() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId.toString(), null);

        controller.revoke(sessionId, authentication);

        verify(sessionService).revokeOwnSession(userId, sessionId);
    }
}
