package id.ac.ui.cs.advprog.bidmart.auth.service.dto;

import id.ac.ui.cs.advprog.bidmart.auth.model.UserRole;

public record RegisterCommand(
    String email,
    String rawPassword,
    UserRole requestedRole
) {
}
