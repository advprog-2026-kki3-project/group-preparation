package id.ac.ui.cs.advprog.bidmart.auth.service.dto;

import java.util.UUID;

public record TwoFactorVerifyCommand(
    UUID challengeId,
    String code,
    String ipAddress,
    String userAgent
) {
}
