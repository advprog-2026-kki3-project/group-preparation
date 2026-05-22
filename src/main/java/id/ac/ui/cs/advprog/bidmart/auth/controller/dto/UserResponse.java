package id.ac.ui.cs.advprog.bidmart.auth.controller.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String primaryRole,
    String status,
    Instant disabledAt
) {
}
