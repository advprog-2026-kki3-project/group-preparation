package id.ac.ui.cs.advprog.bidmart.auth.service;

import id.ac.ui.cs.advprog.bidmart.auth.service.dto.AccessTokenClaims;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.TokenPair;

import java.time.Instant;
import java.util.UUID;

public interface TokenService {
    TokenPair issueTokenPair(UUID userId, UUID sessionId, Instant now, UUID existingTokenFamilyId);

    AccessTokenClaims parseAccessToken(String accessToken);

    String hashRefreshToken(String refreshToken);
}
