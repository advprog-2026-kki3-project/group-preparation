package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.exception.InvalidTwoFactorCodeException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.ResourceNotFoundException;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallenge;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallengePurpose;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorMethod;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserTwoFactorSettings;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthUserRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.TwoFactorChallengeRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.UserTwoFactorSettingsRepository;
import id.ac.ui.cs.advprog.bidmart.auth.service.AuthPolicyService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TokenService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TotpService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TwoFactorCodeSender;
import id.ac.ui.cs.advprog.bidmart.auth.service.TwoFactorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class TwoFactorServiceImpl implements TwoFactorService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthUserRepository authUserRepository;
    private final UserTwoFactorSettingsRepository settingsRepository;
    private final TwoFactorChallengeRepository challengeRepository;
    private final TwoFactorCodeSender codeSender;
    private final TokenService tokenService;
    private final TotpService totpService;
    private final AuthPolicyService authPolicyService;
    private final Clock clock;

    public TwoFactorServiceImpl(
        AuthUserRepository authUserRepository,
        UserTwoFactorSettingsRepository settingsRepository,
        TwoFactorChallengeRepository challengeRepository,
        TwoFactorCodeSender codeSender,
        TokenService tokenService,
        TotpService totpService,
        AuthPolicyService authPolicyService,
        Clock clock
    ) {
        this.authUserRepository = authUserRepository;
        this.settingsRepository = settingsRepository;
        this.challengeRepository = challengeRepository;
        this.codeSender = codeSender;
        this.tokenService = tokenService;
        this.totpService = totpService;
        this.authPolicyService = authPolicyService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public UserTwoFactorSettings getOrCreateSettings(AuthUser user) {
        return settingsRepository.findById(user.getId()).orElseGet(() -> {
            UserTwoFactorSettings settings = new UserTwoFactorSettings();
            settings.setUser(user);
            settings.setEnabled(false);
            return settingsRepository.save(settings);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserTwoFactorSettings> findSettings(UUID userId) {
        return settingsRepository.findById(userId);
    }

    @Override
    @Transactional
    public TwoFactorChallenge createChallenge(
        AuthUser user,
        TwoFactorChallengePurpose purpose,
        TwoFactorMethod method
    ) {
        TwoFactorChallenge challenge = new TwoFactorChallenge();
        challenge.setUser(user);
        challenge.setPurpose(purpose);
        challenge.setMethod(method);
        String code = method == TwoFactorMethod.EMAIL_OTP ? generateOtp() : null;
        challenge.setCodeHash(tokenService.hashRefreshToken(code == null ? UUID.randomUUID().toString() : code));
        challenge.setAttempts(0);
        var policy = authPolicyService.getPolicy();
        challenge.setMaxAttempts(policy.getOtpAttemptLimit());
        challenge.setExpiresAt(Instant.now(clock).plus(policy.getOtpTtl()));
        TwoFactorChallenge saved = challengeRepository.save(challenge);
        if (method == TwoFactorMethod.EMAIL_OTP) {
            codeSender.send(user, method, purpose, code);
        }
        return saved;
    }

    @Override
    @Transactional
    public TwoFactorChallenge consumeChallenge(
        UUID challengeId,
        String code,
        TwoFactorChallengePurpose expectedPurpose
    ) {
        Instant now = Instant.now(clock);
        TwoFactorChallenge challenge = challengeRepository.findByIdAndConsumedAtIsNull(challengeId)
            .orElseThrow(InvalidTwoFactorCodeException::new);

        if (challenge.getPurpose() != expectedPurpose || !challenge.getExpiresAt().isAfter(now)) {
            throw new InvalidTwoFactorCodeException();
        }

        if (challenge.getAttempts() >= challenge.getMaxAttempts()) {
            throw new InvalidTwoFactorCodeException();
        }

        if (!isValidCode(challenge, code, expectedPurpose, now)) {
            challenge.setAttempts(challenge.getAttempts() + 1);
            challengeRepository.save(challenge);
            throw new InvalidTwoFactorCodeException();
        }

        challenge.setConsumedAt(now);
        return challengeRepository.save(challenge);
    }

    @Override
    @Transactional
    public TwoFactorChallenge beginEnable(UUID userId, TwoFactorMethod method) {
        AuthUser user = getUser(userId);
        UserTwoFactorSettings settings = getOrCreateSettings(user);
        settings.setPendingMethod(method);
        settings.setPendingTotpSecret(method == TwoFactorMethod.TOTP ? totpService.generateSecret() : null);
        settingsRepository.save(settings);
        return createChallenge(user, TwoFactorChallengePurpose.ENABLE, method);
    }

    @Override
    @Transactional
    public UserTwoFactorSettings confirmEnable(UUID challengeId, String code) {
        TwoFactorChallenge challenge = consumeChallenge(challengeId, code, TwoFactorChallengePurpose.ENABLE);
        UserTwoFactorSettings settings = getOrCreateSettings(challenge.getUser());
        settings.setEnabled(true);
        settings.setMethod(settings.getPendingMethod() == null ? challenge.getMethod() : settings.getPendingMethod());
        if (settings.getMethod() == TwoFactorMethod.TOTP) {
            settings.setTotpSecret(settings.getPendingTotpSecret());
        }
        settings.setPendingMethod(null);
        settings.setPendingTotpSecret(null);
        return settingsRepository.save(settings);
    }

    @Override
    @Transactional
    public TwoFactorChallenge beginChange(UUID userId, TwoFactorMethod method) {
        AuthUser user = getUser(userId);
        UserTwoFactorSettings settings = getOrCreateSettings(user);
        settings.setPendingMethod(method);
        settings.setPendingTotpSecret(method == TwoFactorMethod.TOTP ? totpService.generateSecret() : null);
        settingsRepository.save(settings);
        return createChallenge(user, TwoFactorChallengePurpose.CHANGE, method);
    }

    @Override
    @Transactional
    public UserTwoFactorSettings confirmChange(UUID challengeId, String code) {
        TwoFactorChallenge challenge = consumeChallenge(challengeId, code, TwoFactorChallengePurpose.CHANGE);
        UserTwoFactorSettings settings = getOrCreateSettings(challenge.getUser());
        settings.setEnabled(true);
        settings.setMethod(settings.getPendingMethod() == null ? challenge.getMethod() : settings.getPendingMethod());
        if (settings.getMethod() == TwoFactorMethod.TOTP) {
            settings.setTotpSecret(settings.getPendingTotpSecret());
        } else {
            settings.setTotpSecret(null);
        }
        settings.setPendingMethod(null);
        settings.setPendingTotpSecret(null);
        return settingsRepository.save(settings);
    }

    @Override
    @Transactional
    public TwoFactorChallenge beginDisable(UUID userId) {
        AuthUser user = getUser(userId);
        UserTwoFactorSettings settings = getOrCreateSettings(user);
        if (!settings.isEnabled()) {
            return createChallenge(user, TwoFactorChallengePurpose.DISABLE, TwoFactorMethod.EMAIL_OTP);
        }
        return createChallenge(user, TwoFactorChallengePurpose.DISABLE, settings.getMethod());
    }

    @Override
    @Transactional
    public UserTwoFactorSettings confirmDisable(UUID challengeId, String code) {
        TwoFactorChallenge challenge = consumeChallenge(challengeId, code, TwoFactorChallengePurpose.DISABLE);
        UserTwoFactorSettings settings = getOrCreateSettings(challenge.getUser());
        settings.setEnabled(false);
        settings.setMethod(null);
        settings.setPendingMethod(null);
        settings.setTotpSecret(null);
        settings.setPendingTotpSecret(null);
        return settingsRepository.save(settings);
    }

    private boolean isValidCode(
        TwoFactorChallenge challenge,
        String code,
        TwoFactorChallengePurpose expectedPurpose,
        Instant now
    ) {
        if (challenge.getMethod() == TwoFactorMethod.EMAIL_OTP) {
            return challenge.getCodeHash().equals(tokenService.hashRefreshToken(code));
        }

        UserTwoFactorSettings settings = getOrCreateSettings(challenge.getUser());
        String secret = switch (expectedPurpose) {
            case ENABLE, CHANGE -> settings.getPendingTotpSecret();
            case LOGIN, DISABLE -> settings.getTotpSecret();
        };
        return totpService.verify(secret, code, now);
    }

    private AuthUser getUser(UUID userId) {
        return authUserRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private String generateOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }
}
