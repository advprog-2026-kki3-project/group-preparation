package id.ac.ui.cs.advprog.bidmart.auth.service.dto;

import java.time.Instant;
import java.util.UUID;

public record AccessTokenClaims(
    UUID userId,
    UUID sessionId,
    Instant expiresAt
) {
}
