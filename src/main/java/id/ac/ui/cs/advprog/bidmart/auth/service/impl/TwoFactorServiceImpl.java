package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.exception.InvalidTwoFactorCodeException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.OtpAttemptLimitExceededException;
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
import id.ac.ui.cs.advprog.bidmart.auth.service.TwoFactorChallengeAttemptService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TwoFactorCodeSender;
import id.ac.ui.cs.advprog.bidmart.auth.service.TwoFactorService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class TwoFactorServiceImpl implements TwoFactorService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthUserRepository authUserRepository;
    private final UserTwoFactorSettingsRepository settingsRepository;
    private final TwoFactorChallengeRepository challengeRepository;
    private final TwoFactorChallengeAttemptService challengeAttemptService;
    private final TwoFactorCodeSender codeSender;
    private final TokenService tokenService;
    private final TotpService totpService;
    private final AuthPolicyService authPolicyService;
    private final Clock clock;
    private final MeterRegistry meterRegistry;
    private final Counter twoFactorVerifySuccesses;
    private final Counter twoFactorVerifyFailures;
    private final Counter twoFactorAttemptLimitExceeded;
    private final Counter twoFactorEnabled;
    private final Counter twoFactorDisabled;

    public TwoFactorServiceImpl(
        AuthUserRepository authUserRepository,
        UserTwoFactorSettingsRepository settingsRepository,
        TwoFactorChallengeRepository challengeRepository,
        TwoFactorChallengeAttemptService challengeAttemptService,
        TwoFactorCodeSender codeSender,
        TokenService tokenService,
        TotpService totpService,
        AuthPolicyService authPolicyService,
        Clock clock,
        MeterRegistry meterRegistry
    ) {
        this.authUserRepository = authUserRepository;
        this.settingsRepository = settingsRepository;
        this.challengeRepository = challengeRepository;
        this.challengeAttemptService = challengeAttemptService;
        this.codeSender = codeSender;
        this.tokenService = tokenService;
        this.totpService = totpService;
        this.authPolicyService = authPolicyService;
        this.clock = clock;
        this.meterRegistry = meterRegistry;
        this.twoFactorVerifySuccesses = Counter.builder("bidmart.auth.2fa.verify.success")
            .description("Total successful 2FA verifications")
            .register(meterRegistry);
        this.twoFactorVerifyFailures = Counter.builder("bidmart.auth.2fa.verify.failure")
            .description("Total failed 2FA verifications")
            .register(meterRegistry);
        this.twoFactorAttemptLimitExceeded = Counter.builder("bidmart.auth.2fa.attempt_limit_exceeded")
            .description("Total 2FA verifications blocked by attempt limits")
            .register(meterRegistry);
        this.twoFactorEnabled = Counter.builder("bidmart.auth.2fa.enabled")
            .description("Total users who enabled 2FA")
            .register(meterRegistry);
        this.twoFactorDisabled = Counter.builder("bidmart.auth.2fa.disabled")
            .description("Total users who disabled 2FA")
            .register(meterRegistry);
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
        Counter.builder("bidmart.auth.2fa.challenges.created")
            .description("Total 2FA challenges created")
            .tag("purpose", purpose.name())
            .tag("method", method.name())
            .register(meterRegistry)
            .increment();
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
            .orElseThrow(() -> {
                twoFactorVerifyFailures.increment();
                return new InvalidTwoFactorCodeException();
            });

        if (challenge.getPurpose() != expectedPurpose || !challenge.getExpiresAt().isAfter(now)) {
            twoFactorVerifyFailures.increment();
            throw new InvalidTwoFactorCodeException();
        }

        enforceOtpAttemptLimit(challenge, now);

        if (!isValidCode(challenge, code, expectedPurpose, now)) {
            challengeAttemptService.recordFailedAttempt(challenge.getId());
            twoFactorVerifyFailures.increment();
            throw new InvalidTwoFactorCodeException();
        }

        challenge.setConsumedAt(now);
        TwoFactorChallenge savedChallenge = challengeRepository.save(challenge);
        twoFactorVerifySuccesses.increment();
        return savedChallenge;
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
        UserTwoFactorSettings savedSettings = settingsRepository.save(settings);
        twoFactorEnabled.increment();
        return savedSettings;
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
        UserTwoFactorSettings savedSettings = settingsRepository.save(settings);
        twoFactorEnabled.increment();
        return savedSettings;
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
        UserTwoFactorSettings savedSettings = settingsRepository.save(settings);
        twoFactorDisabled.increment();
        return savedSettings;
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

    private void enforceOtpAttemptLimit(TwoFactorChallenge challenge, Instant now) {
        var policy = authPolicyService.getPolicy();
        Instant windowStart = now.minus(policy.getOtpTtl());
        long failedAttempts = challengeRepository.sumAttemptsByUserAndPurposeSince(
            challenge.getUser().getId(),
            challenge.getPurpose(),
            windowStart
        );
        if (failedAttempts >= policy.getOtpAttemptLimit()) {
            long retryAfterSeconds = challengeRepository
                .findFirstByUser_IdAndPurposeAndAttemptsGreaterThanAndCreatedAtAfterOrderByCreatedAtAsc(
                    challenge.getUser().getId(),
                    challenge.getPurpose(),
                    0,
                    windowStart
                )
                .map(oldestChallenge -> Duration.between(
                    now,
                    oldestChallenge.getCreatedAt().plus(policy.getOtpTtl())
                ).toSeconds())
                .orElse(policy.getOtpTtl().toSeconds());
            twoFactorAttemptLimitExceeded.increment();
            throw new OtpAttemptLimitExceededException(retryAfterSeconds);
        }
    }

    private String generateOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }
}
