package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotpServiceImplTest {
    private final TotpServiceImpl totpService = new TotpServiceImpl();

    @Test
    void verifiesKnownTotpCode() {
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        assertTrue(totpService.verify(secret, "287082", Instant.ofEpochSecond(59)));
    }

    @Test
    void rejectsMalformedOrIncorrectCode() {
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";
        Instant now = Instant.ofEpochSecond(59);

        assertFalse(totpService.verify(secret, "abcdef", now));
        assertFalse(totpService.verify(secret, "111111", now));
    }
}
