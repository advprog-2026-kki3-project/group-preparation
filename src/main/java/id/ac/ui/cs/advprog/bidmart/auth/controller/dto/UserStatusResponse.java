package id.ac.ui.cs.advprog.bidmart.auth.controller.dto;

import java.time.Instant;
import java.util.UUID;

public record UserStatusResponse(
    UUID id,
    String status,
    Instant disabledAt
) {
}
