package id.ac.ui.cs.advprog.bidmart.auth.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TwoFactorVerifyRequest(
    @NotNull UUID challengeId,
    @NotBlank String code
) {
}
