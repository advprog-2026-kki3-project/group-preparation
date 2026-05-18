package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.AccessTokenClaims;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.TokenPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenServiceImplTest {
    private TokenServiceImpl tokenService;

    @BeforeEach
    void setUp() {
        AuthProperties properties = new AuthProperties();
        properties.setAccessTokenTtl(Duration.ofMinutes(15));
        properties.setRefreshTokenTtl(Duration.ofDays(30));
        properties.setJwtIssuer("bidmart-auth-test");
        properties.setJwtAccessSecret("test-secret-at-least-32-characters-long");
        tokenService = new TokenServiceImpl(properties);
    }

    @Test
    void issueAndParseAccessTokenWorks() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        Instant now = Instant.now();

        TokenPair pair = tokenService.issueTokenPair(userId, sessionId, now, null);
        AccessTokenClaims claims = tokenService.parseAccessToken(pair.accessToken());

        assertEquals(userId, claims.userId());
        assertEquals(sessionId, claims.sessionId());
        assertEquals(now.plus(Duration.ofMinutes(15)).getEpochSecond(), claims.expiresAt().getEpochSecond());
        assertNotNull(pair.refreshToken());
        assertTrue(pair.refreshTokenHash().length() >= 64);
    }
}
