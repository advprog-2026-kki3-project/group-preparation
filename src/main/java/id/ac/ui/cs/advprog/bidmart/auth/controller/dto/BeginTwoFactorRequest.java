package id.ac.ui.cs.advprog.bidmart.auth.controller.dto;

import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorMethod;
import jakarta.validation.constraints.NotNull;

public record BeginTwoFactorRequest(
    @NotNull TwoFactorMethod method
) {
}
