package id.ac.ui.cs.advprog.bidmart.auth.controller.dto;

import java.util.UUID;

public record RoleResponse(
    UUID id,
    String name,
    String description,
    boolean systemRole
) {
}
