package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import id.ac.ui.cs.advprog.bidmart.auth.exception.InvalidAccessTokenException;
import id.ac.ui.cs.advprog.bidmart.auth.service.TokenService;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.AccessTokenClaims;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.TokenPair;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String SESSION_ID_CLAIM = "session_id";

    private final AuthProperties authProperties;
    private final SecretKey accessSecretKey;

    public TokenServiceImpl(AuthProperties authProperties) {
        this.authProperties = authProperties;
        byte[] keyMaterial = authProperties.getJwtAccessSecret().getBytes(StandardCharsets.UTF_8);
        if (keyMaterial.length < 32) {
            throw new IllegalArgumentException("JWT access secret must be at least 32 bytes for HS256.");
        }
        this.accessSecretKey = Keys.hmacShaKeyFor(keyMaterial);
    }

    @Override
    public TokenPair issueTokenPair(UUID userId, UUID sessionId, Instant now, UUID existingTokenFamilyId) {
        Instant accessExpiresAt = now.plus(authProperties.getAccessTokenTtl());
        Instant refreshExpiresAt = now.plus(authProperties.getRefreshTokenTtl());
        UUID tokenFamilyId = existingTokenFamilyId == null ? UUID.randomUUID() : existingTokenFamilyId;

        String accessToken = Jwts.builder()
            .issuer(authProperties.getJwtIssuer())
            .subject(userId.toString())
            .claim(SESSION_ID_CLAIM, sessionId.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(accessExpiresAt))
            .signWith(accessSecretKey)
            .compact();

        String refreshToken = generateOpaqueRefreshToken();
        String refreshTokenHash = hashRefreshToken(refreshToken);

        return new TokenPair(
            accessToken,
            accessExpiresAt,
            refreshToken,
            refreshTokenHash,
            refreshExpiresAt,
            tokenFamilyId
        );
    }

    @Override
    public AccessTokenClaims parseAccessToken(String accessToken) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(accessSecretKey)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

            UUID userId = UUID.fromString(claims.getSubject());
            UUID sessionId = UUID.fromString(claims.get(SESSION_ID_CLAIM, String.class));
            Instant expiresAt = claims.getExpiration().toInstant();
            return new AccessTokenClaims(userId, sessionId, expiresAt);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidAccessTokenException();
        }
    }

    @Override
    public String hashRefreshToken(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }

    private String generateOpaqueRefreshToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
