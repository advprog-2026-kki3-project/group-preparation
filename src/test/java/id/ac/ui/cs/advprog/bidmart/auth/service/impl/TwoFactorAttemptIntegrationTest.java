package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import id.ac.ui.cs.advprog.bidmart.auth.exception.InvalidTwoFactorCodeException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.OtpAttemptLimitExceededException;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallenge;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallengePurpose;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorMethod;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserStatus;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthUserRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.TwoFactorChallengeRepository;
import id.ac.ui.cs.advprog.bidmart.auth.service.AuthPolicyService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TokenService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TwoFactorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class TwoFactorAttemptIntegrationTest {
    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private TwoFactorChallengeRepository challengeRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TwoFactorService twoFactorService;

    @Autowired
    private AuthPolicyService authPolicyService;

    @Test
    void failedOtpAttemptsPersistAndBlockCorrectCodeAfterLimit() {
        setOtpAttemptPolicy(5);
        AuthUser savedUser = createUser("otp-attempt-");
        TwoFactorChallenge savedChallenge = createChallenge(savedUser, "123456");

        for (int attempt = 0; attempt < 5; attempt++) {
            assertThrows(
                InvalidTwoFactorCodeException.class,
                () -> twoFactorService.consumeChallenge(savedChallenge.getId(), "000000", TwoFactorChallengePurpose.LOGIN)
            );
        }

        TwoFactorChallenge afterFailures = challengeRepository.findById(savedChallenge.getId()).orElseThrow();
        assertEquals(5, afterFailures.getAttempts());
        assertThrows(
            OtpAttemptLimitExceededException.class,
            () -> twoFactorService.consumeChallenge(savedChallenge.getId(), "123456", TwoFactorChallengePurpose.LOGIN)
        );
    }

    @Test
    void correctOtpStillWorksAfterOneWrongAttempt() {
        setOtpAttemptPolicy(5);
        AuthUser savedUser = createUser("otp-retry-");
        TwoFactorChallenge savedChallenge = createChallenge(savedUser, "123456");

        assertThrows(
            InvalidTwoFactorCodeException.class,
            () -> twoFactorService.consumeChallenge(savedChallenge.getId(), "000000", TwoFactorChallengePurpose.LOGIN)
        );

        TwoFactorChallenge consumed = twoFactorService.consumeChallenge(
            savedChallenge.getId(),
            "123456",
            TwoFactorChallengePurpose.LOGIN
        );

        assertNotNull(consumed.getConsumedAt());
    }

    @Test
    void otpAttemptLimitAppliesAcrossNewChallengesWithinTtl() {
        setOtpAttemptPolicy(3);
        AuthUser savedUser = createUser("otp-window-");

        for (int attempt = 0; attempt < 3; attempt++) {
            TwoFactorChallenge challenge = createChallenge(savedUser, "123456");
            assertThrows(
                InvalidTwoFactorCodeException.class,
                () -> twoFactorService.consumeChallenge(challenge.getId(), "000000", TwoFactorChallengePurpose.LOGIN)
            );
        }

        TwoFactorChallenge fourthChallenge = createChallenge(savedUser, "123456");
        assertThrows(
            OtpAttemptLimitExceededException.class,
            () -> twoFactorService.consumeChallenge(fourthChallenge.getId(), "123456", TwoFactorChallengePurpose.LOGIN)
        );
    }

    private AuthUser createUser(String emailPrefix) {
        AuthUser user = new AuthUser();
        user.setEmail(emailPrefix + UUID.randomUUID() + "@example.com");
        user.setPasswordHash("unused");
        user.setPrimaryRole(UserRole.BUYER);
        user.setStatus(UserStatus.ACTIVE);
        return authUserRepository.save(user);
    }

    private TwoFactorChallenge createChallenge(AuthUser user, String code) {
        TwoFactorChallenge challenge = new TwoFactorChallenge();
        challenge.setUser(user);
        challenge.setPurpose(TwoFactorChallengePurpose.LOGIN);
        challenge.setMethod(TwoFactorMethod.EMAIL_OTP);
        challenge.setCodeHash(tokenService.hashRefreshToken(code));
        challenge.setAttempts(0);
        challenge.setMaxAttempts(5);
        challenge.setExpiresAt(Instant.now().plusSeconds(300));
        return challengeRepository.save(challenge);
    }

    private void setOtpAttemptPolicy(int otpAttemptLimit) {
        authPolicyService.updatePolicy(
            3,
            AuthProperties.ConcurrentSessionPolicy.REVOKE_OLDEST,
            5,
            Duration.ofMinutes(15),
            otpAttemptLimit,
            Duration.ofMinutes(5)
        );
    }
}
