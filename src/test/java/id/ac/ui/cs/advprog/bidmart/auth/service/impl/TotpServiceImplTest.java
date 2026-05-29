package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotpServiceImplTest {
    private final TotpServiceImpl totpService = new TotpServiceImpl();

    @Test
    void verifiesKnownTotpCode() {
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        assertTrue(totpService.verify(secret, "287082", Instant.ofEpochSecond(59)));
    }

    @Test
    void verifiesCodeWithinAllowedAdjacentWindow() {
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        assertTrue(totpService.verify(secret, "287082", Instant.ofEpochSecond(29)));
        assertTrue(totpService.verify(secret, "287082", Instant.ofEpochSecond(89)));
        assertFalse(totpService.verify(secret, "287082", Instant.ofEpochSecond(90)));
    }

    @Test
    void rejectsMalformedOrIncorrectCode() {
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";
        Instant now = Instant.ofEpochSecond(59);

        assertFalse(totpService.verify(null, "287082", now));
        assertFalse(totpService.verify(secret, null, now));
        assertFalse(totpService.verify(secret, "abcdef", now));
        assertFalse(totpService.verify(secret, "111111", now));
    }

    @Test
    void rejectsInvalidBase32Secret() {
        assertFalse(totpService.verify("NOT-BASE32", "123456", Instant.ofEpochSecond(59)));
    }

    @Test
    void generatesBase32Secret() {
        String secret = totpService.generateSecret();

        assertEquals(32, secret.length());
        assertTrue(secret.matches("[A-Z2-7]+"));
    }

    @Test
    void buildsOtpAuthUriWithEncodedValues() {
        String uri = totpService.buildOtpAuthUri("Bid Mart", "buyer+one@example.com", "ABC123");

        assertEquals(
            "otpauth://totp/Bid%20Mart%3Abuyer%2Bone%40example.com"
                + "?secret=ABC123&issuer=Bid%20Mart&algorithm=SHA1&digits=6&period=30",
            uri
        );
    }
}
