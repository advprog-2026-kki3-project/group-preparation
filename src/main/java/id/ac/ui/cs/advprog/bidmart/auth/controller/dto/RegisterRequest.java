package id.ac.ui.cs.advprog.bidmart.auth.controller.dto;

import id.ac.ui.cs.advprog.bidmart.auth.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank String password,
    @NotNull UserRole role
) {
}
