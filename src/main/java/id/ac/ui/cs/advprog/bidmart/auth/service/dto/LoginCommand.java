package id.ac.ui.cs.advprog.bidmart.auth.service.dto;

public record LoginCommand(
    String email,
    String rawPassword,
    String ipAddress,
    String userAgent
) {
}
