package id.ac.ui.cs.advprog.bidmart.auth.controller.dto;

import java.time.Instant;
import java.util.UUID;

public record TwoFactorChallengeResponse(
    UUID challengeId,
    String method,
    Instant expiresAt,
    String totpSecret,
    String totpUri
) {
}
