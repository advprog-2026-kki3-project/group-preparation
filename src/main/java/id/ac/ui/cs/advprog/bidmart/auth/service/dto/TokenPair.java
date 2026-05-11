package id.ac.ui.cs.advprog.bidmart.auth.service.dto;

import java.time.Instant;
import java.util.UUID;

public record TokenPair(
    String accessToken,
    Instant accessExpiresAt,
    String refreshToken,
    String refreshTokenHash,
    Instant refreshExpiresAt,
    UUID tokenFamilyId
) {
}
