package id.ac.ui.cs.advprog.bidmart.auth.service;

import java.time.Instant;

public interface TotpService {
    String generateSecret();

    String buildOtpAuthUri(String issuer, String accountName, String secret);

    boolean verify(String secret, String code, Instant now);
}
