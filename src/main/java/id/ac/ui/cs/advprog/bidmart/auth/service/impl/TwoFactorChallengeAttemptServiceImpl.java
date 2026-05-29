package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.repository.TwoFactorChallengeRepository;
import id.ac.ui.cs.advprog.bidmart.auth.service.TwoFactorChallengeAttemptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TwoFactorChallengeAttemptServiceImpl implements TwoFactorChallengeAttemptService {
    private final TwoFactorChallengeRepository challengeRepository;

    public TwoFactorChallengeAttemptServiceImpl(TwoFactorChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedAttempt(UUID challengeId) {
        challengeRepository.incrementAttempts(challengeId);
    }
}
