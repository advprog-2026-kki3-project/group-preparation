package id.ac.ui.cs.advprog.bidmart.auth.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePermissionRequest(
    @NotBlank String name,
    String description
) {
}
