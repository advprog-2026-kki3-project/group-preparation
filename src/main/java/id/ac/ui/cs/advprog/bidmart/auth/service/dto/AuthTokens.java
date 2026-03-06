package id.ac.ui.cs.advprog.bidmart.auth.service.dto;

import java.time.Instant;

public record AuthTokens(
    String accessToken,
    String refreshToken,
    Instant accessExpiresAt,
    Instant refreshExpiresAt
) {
}
