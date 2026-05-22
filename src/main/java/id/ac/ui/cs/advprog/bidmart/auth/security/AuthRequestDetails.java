package id.ac.ui.cs.advprog.bidmart.auth.security;

import java.util.UUID;

public record AuthRequestDetails(
    UUID sessionId,
    boolean twoFactorVerified
) {
}
