package id.ac.ui.cs.advprog.bidmart.auth.controller.dto;

import java.time.Instant;

public record ErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path
) {
}
