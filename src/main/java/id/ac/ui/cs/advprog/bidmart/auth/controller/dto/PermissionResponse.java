package id.ac.ui.cs.advprog.bidmart.auth.controller.dto;

import java.util.UUID;

public record PermissionResponse(
    UUID id,
    String name,
    String description
) {
}
