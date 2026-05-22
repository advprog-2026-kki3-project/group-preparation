package id.ac.ui.cs.advprog.bidmart.auth.service;

public interface CredentialService {
    void validatePasswordPolicy(String rawPassword);

    String hashPassword(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
