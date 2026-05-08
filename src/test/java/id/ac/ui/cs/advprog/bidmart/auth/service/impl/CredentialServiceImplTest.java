package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.exception.PasswordPolicyViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CredentialServiceImplTest {
    private CredentialServiceImpl credentialService;

    @BeforeEach
    void setUp() {
        credentialService = new CredentialServiceImpl(Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
    }

    @Test
    void validatePasswordPolicyRejectsShortPassword() {
        assertThrows(PasswordPolicyViolationException.class, () -> credentialService.validatePasswordPolicy("short"));
    }

    @Test
    void hashPasswordAndMatchesWorks() {
        String raw = "VeryStrongPass123!";
        assertDoesNotThrow(() -> credentialService.validatePasswordPolicy(raw));
        String hash = credentialService.hashPassword(raw);
        assertTrue(credentialService.matches(raw, hash));
    }
}
