package id.ac.ui.cs.advprog.bidmart.auth.service.dto;

import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorMethod;

import java.util.UUID;

public record LoginResult(
    AuthTokens tokens,
    UUID challengeId,
    TwoFactorMethod method
) {
    public static LoginResult authenticated(AuthTokens tokens) {
        return new LoginResult(tokens, null, null);
    }

    public static LoginResult twoFactorRequired(UUID challengeId, TwoFactorMethod method) {
        return new LoginResult(null, challengeId, method);
    }

    public boolean requiresTwoFactor() {
        return challengeId != null;
    }
}
