package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.service.TotpService;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;

@Service
public class TotpServiceImpl implements TotpService {
    private static final char[] BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
    private static final int SECRET_BYTES = 20;
    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final int ALLOWED_WINDOW_STEPS = 1;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public String generateSecret() {
        byte[] random = new byte[SECRET_BYTES];
        SECURE_RANDOM.nextBytes(random);
        return encodeBase32(random);
    }

    @Override
    public String buildOtpAuthUri(String issuer, String accountName, String secret) {
        String label = encodeUri(issuer + ":" + accountName);
        return "otpauth://totp/" + label
            + "?secret=" + encodeUri(secret)
            + "&issuer=" + encodeUri(issuer)
            + "&algorithm=SHA1&digits=" + CODE_DIGITS
            + "&period=" + TIME_STEP_SECONDS;
    }

    @Override
    public boolean verify(String secret, String code, Instant now) {
        if (secret == null || code == null || !code.matches("\\d{" + CODE_DIGITS + "}")) {
            return false;
        }

        long counter = now.getEpochSecond() / TIME_STEP_SECONDS;
        for (int offset = -ALLOWED_WINDOW_STEPS; offset <= ALLOWED_WINDOW_STEPS; offset++) {
            if (constantTimeEquals(generateCodeOrNull(secret, counter + offset), code)) {
                return true;
            }
        }
        return false;
    }

    private String generateCodeOrNull(String secret, long counter) {
        try {
            return generateCode(secret, counter);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String generateCode(String secret, long counter) {
        try {
            byte[] key = decodeBase32(secret);
            byte[] counterBytes = ByteBuffer.allocate(Long.BYTES).putLong(counter).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(counterBytes);
            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);
            int otp = binary % 1_000_000;
            return String.format(Locale.ROOT, "%06d", otp);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Could not generate TOTP code.", exception);
        }
    }

    private String encodeBase32(byte[] bytes) {
        StringBuilder encoded = new StringBuilder((bytes.length * 8 + 4) / 5);
        int buffer = 0;
        int bitsLeft = 0;
        for (byte value : bytes) {
            buffer = (buffer << 8) | (value & 0xff);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                encoded.append(BASE32_ALPHABET[(buffer >> (bitsLeft - 5)) & 0x1f]);
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) {
            encoded.append(BASE32_ALPHABET[(buffer << (5 - bitsLeft)) & 0x1f]);
        }
        return encoded.toString();
    }

    private byte[] decodeBase32(String value) {
        String normalized = value.replace("=", "").replace(" ", "").toUpperCase(Locale.ROOT);
        byte[] output = new byte[normalized.length() * 5 / 8];
        int buffer = 0;
        int bitsLeft = 0;
        int index = 0;
        for (char character : normalized.toCharArray()) {
            int digit = base32Index(character);
            if (digit < 0) {
                throw new IllegalArgumentException("Invalid base32 TOTP secret.");
            }
            buffer = (buffer << 5) | digit;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                output[index++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xff);
                bitsLeft -= 8;
            }
        }
        return output;
    }

    private int base32Index(char character) {
        for (int index = 0; index < BASE32_ALPHABET.length; index++) {
            if (BASE32_ALPHABET[index] == character) {
                return index;
            }
        }
        return -1;
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigestHolder.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }

    private String encodeUri(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static final class MessageDigestHolder {
        private MessageDigestHolder() {
        }

        private static boolean isEqual(byte[] left, byte[] right) {
            return java.security.MessageDigest.isEqual(left, right);
        }
    }
}
