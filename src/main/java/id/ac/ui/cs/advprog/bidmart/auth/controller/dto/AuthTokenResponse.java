package id.ac.ui.cs.advprog.bidmart.auth.controller.dto;

import java.time.Instant;

public record AuthTokenResponse(
    String tokenType,
    String accessToken,
    String refreshToken,
    Instant accessExpiresAt,
    Instant refreshExpiresAt
) {
}
