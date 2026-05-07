package id.ac.ui.cs.advprog.bidmart.auth.controller;

import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.AuthSessionResponse;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthSession;
import id.ac.ui.cs.advprog.bidmart.auth.security.RequiresPermission;
import id.ac.ui.cs.advprog.bidmart.auth.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/sessions")
@RequiresPermission
public class SessionController {
    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public List<AuthSessionResponse> list(Authentication authentication) {
        return sessionService.listActiveSessions(currentUserId(authentication))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> revoke(@PathVariable UUID sessionId, Authentication authentication) {
        sessionService.revokeOwnSession(currentUserId(authentication), sessionId);
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private AuthSessionResponse toResponse(AuthSession session) {
        return new AuthSessionResponse(
            session.getId(),
            session.getIpAddress(),
            session.getUserAgent(),
            session.getCreatedAt(),
            session.getLastSeenAt(),
            session.getExpiresAt()
        );
    }
}
