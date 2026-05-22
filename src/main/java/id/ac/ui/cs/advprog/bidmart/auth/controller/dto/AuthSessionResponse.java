package id.ac.ui.cs.advprog.bidmart.auth.controller.dto;

import java.time.Instant;
import java.util.UUID;

public record AuthSessionResponse(
    UUID id,
    String ipAddress,
    String userAgent,
    Instant createdAt,
    Instant lastSeenAt,
    Instant expiresAt
) {
}
