package id.ac.ui.cs.advprog.bidmart.auth.controller.dto;

import java.time.Instant;
import java.util.UUID;

public record LoginResponse(
    String tokenType,
    String accessToken,
    String refreshToken,
    Instant accessExpiresAt,
    Instant refreshExpiresAt,
    boolean requiresTwoFactor,
    UUID challengeId,
    String twoFactorMethod
) {
}
