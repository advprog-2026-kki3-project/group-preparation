package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.exception.PasswordPolicyViolationException;
import id.ac.ui.cs.advprog.bidmart.auth.service.CredentialService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CredentialServiceImpl implements CredentialService {
    private final PasswordEncoder passwordEncoder;

    public CredentialServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void validatePasswordPolicy(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new PasswordPolicyViolationException("Password must be at least 8 characters.");
        }
    }

    @Override
    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
