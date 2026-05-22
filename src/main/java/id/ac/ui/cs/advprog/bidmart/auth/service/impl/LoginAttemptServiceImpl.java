package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.model.LoginAttempt;
import id.ac.ui.cs.advprog.bidmart.auth.repository.LoginAttemptRepository;
import id.ac.ui.cs.advprog.bidmart.auth.service.LoginAttemptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {
    private final LoginAttemptRepository loginAttemptRepository;
    private final Clock clock;

    public LoginAttemptServiceImpl(LoginAttemptRepository loginAttemptRepository, Clock clock) {
        this.loginAttemptRepository = loginAttemptRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public long countFailedAttemptsSince(String email, Instant attemptedAfter) {
        return loginAttemptRepository.countByEmailAndSuccessfulFalseAndAttemptedAtAfter(email, attemptedAfter);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Instant> findOldestFailedAttemptSince(String email, Instant attemptedAfter) {
        return loginAttemptRepository
            .findFirstByEmailAndSuccessfulFalseAndAttemptedAtAfterOrderByAttemptedAtAsc(email, attemptedAfter)
            .map(LoginAttempt::getAttemptedAt);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAttempt(String email, String ipAddress, boolean successful) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setEmail(email);
        attempt.setIpAddress(ipAddress);
        attempt.setSuccessful(successful);
        attempt.setAttemptedAt(Instant.now(clock));
        loginAttemptRepository.save(attempt);
    }
}
