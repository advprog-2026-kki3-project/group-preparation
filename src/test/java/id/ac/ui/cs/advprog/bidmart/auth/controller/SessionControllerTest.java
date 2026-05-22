package id.ac.ui.cs.advprog.bidmart.auth.controller;

import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.AuthSessionResponse;
import id.ac.ui.cs.advprog.bidmart.auth.exception.InvalidAccessTokenException;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthSession;
import id.ac.ui.cs.advprog.bidmart.auth.security.AuthRequestDetails;
import id.ac.ui.cs.advprog.bidmart.auth.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        authentication.setDetails(new AuthRequestDetails(sessionId, true));

        var response = controller.revokeCurrent(authentication);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(sessionService).revokeOwnSession(userId, sessionId);
    }

    @Test
    void revokeSpecificSessionStillUsesRequestedSessionId() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId.toString(), null);

        var response = controller.revoke(sessionId, authentication);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(sessionService).revokeOwnSession(userId, sessionId);
    }

    @Test
    void listMapsActiveSessions() {
        UUID userId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId.toString(), null);
        AuthSession session = session();
        when(sessionService.listActiveSessions(userId)).thenReturn(List.of(session));

        List<AuthSessionResponse> response = controller.list(authentication);

        assertEquals(1, response.size());
        assertEquals(session.getId(), response.getFirst().id());
        assertEquals("127.0.0.1", response.getFirst().ipAddress());
        assertEquals("JUnit", response.getFirst().userAgent());
        assertEquals(session.getCreatedAt(), response.getFirst().createdAt());
        assertEquals(session.getLastSeenAt(), response.getFirst().lastSeenAt());
        assertEquals(session.getExpiresAt(), response.getFirst().expiresAt());
    }

    @Test
    void revokeCurrentRejectsAuthenticationWithoutSessionDetails() {
        UUID userId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId.toString(), null);

        assertThrows(InvalidAccessTokenException.class, () -> controller.revokeCurrent(authentication));
    }

    private AuthSession session() {
        AuthSession session = new AuthSession();
        ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(session, "createdAt", Instant.parse("2026-05-21T10:00:00Z"));
        ReflectionTestUtils.setField(session, "lastSeenAt", Instant.parse("2026-05-21T10:05:00Z"));
        session.setIpAddress("127.0.0.1");
        session.setUserAgent("JUnit");
        session.setExpiresAt(Instant.parse("2026-05-21T11:00:00Z"));
        return session;
    }
}
