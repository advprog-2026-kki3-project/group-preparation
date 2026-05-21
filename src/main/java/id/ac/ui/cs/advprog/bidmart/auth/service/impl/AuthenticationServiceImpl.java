package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import id.ac.ui.cs.advprog.bidmart.auth.exception.EmailAlreadyUsedException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.InvalidCredentialsException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.InvalidRefreshTokenException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.LoginAttemptLimitExceededException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.ResourceNotFoundException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.UserDisabledException;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthSession;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPolicySettings;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUserRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.RefreshToken;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallenge;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallengePurpose;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserStatus;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserTwoFactorSettings;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthRoleRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthUserRoleRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthUserRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.RefreshTokenRepository;
import id.ac.ui.cs.advprog.bidmart.auth.service.AuthenticationService;
import id.ac.ui.cs.advprog.bidmart.auth.service.AuthPolicyService;
import id.ac.ui.cs.advprog.bidmart.auth.service.CredentialService;
import id.ac.ui.cs.advprog.bidmart.auth.service.LoginAttemptService;
import id.ac.ui.cs.advprog.bidmart.auth.service.SessionService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TokenService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TwoFactorService;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.AuthTokens;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.LoginCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.LoginResult;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.RefreshCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.RegisterCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.TwoFactorVerifyCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.TokenPair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthUserRepository authUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthRoleRepository authRoleRepository;
    private final AuthUserRoleRepository authUserRoleRepository;
    private final AuthPolicyService authPolicyService;
    private final CredentialService credentialService;
    private final LoginAttemptService loginAttemptService;
    private final SessionService sessionService;
    private final TokenService tokenService;
    private final TwoFactorService twoFactorService;
    private final AuthProperties authProperties;
    private final Clock clock;

    public AuthenticationServiceImpl(
        AuthUserRepository authUserRepository,
        RefreshTokenRepository refreshTokenRepository,
        AuthRoleRepository authRoleRepository,
        AuthUserRoleRepository authUserRoleRepository,
        AuthPolicyService authPolicyService,
        CredentialService credentialService,
        LoginAttemptService loginAttemptService,
        SessionService sessionService,
        TokenService tokenService,
        TwoFactorService twoFactorService,
        AuthProperties authProperties,
        Clock clock
    ) {
        this.authUserRepository = authUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authRoleRepository = authRoleRepository;
        this.authUserRoleRepository = authUserRoleRepository;
        this.authPolicyService = authPolicyService;
        this.credentialService = credentialService;
        this.loginAttemptService = loginAttemptService;
        this.sessionService = sessionService;
        this.tokenService = tokenService;
        this.twoFactorService = twoFactorService;
        this.authProperties = authProperties;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void register(RegisterCommand command) {
        requireNonBlank(command.email(), "Email is required.");
        requireNonBlank(command.rawPassword(), "Password is required.");

        String email = normalizeEmail(command.email());
        if (authUserRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyUsedException();
        }

        credentialService.validatePasswordPolicy(command.rawPassword());
        UserRole requestedRole = command.requestedRole() == null ? UserRole.BUYER : command.requestedRole();
        if (requestedRole == UserRole.ADMINISTRATOR && authUserRepository.count() > 0) {
            requestedRole = UserRole.BUYER;
        }

        AuthUser user = new AuthUser();
        user.setEmail(email);
        user.setPasswordHash(credentialService.hashPassword(command.rawPassword()));
        user.setPrimaryRole(requestedRole);
        user.setStatus(UserStatus.ACTIVE);
        AuthUser savedUser = authUserRepository.save(user);
        AuthRole role = authRoleRepository.findByNameIgnoreCase(requestedRole.name())
            .orElseThrow(() -> new ResourceNotFoundException("Default role not found."));
        authUserRoleRepository.save(new AuthUserRole(savedUser.getId(), role.getId()));
    }

    @Override
    @Transactional
    public LoginResult login(LoginCommand command) {
        requireNonBlank(command.email(), "Email is required.");
        requireNonBlank(command.rawPassword(), "Password is required.");

        String email = normalizeEmail(command.email());
        enforceLoginAttemptLimit(email);
        AuthUser user = authUserRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> {
                recordLoginAttempt(email, command.ipAddress(), false);
                return new InvalidCredentialsException();
            });

        if (user.getStatus() == UserStatus.DISABLED) {
            recordLoginAttempt(email, command.ipAddress(), false);
            throw new UserDisabledException();
        }

        if (!credentialService.matches(command.rawPassword(), user.getPasswordHash())) {
            recordLoginAttempt(email, command.ipAddress(), false);
            throw new InvalidCredentialsException();
        }

        recordLoginAttempt(email, command.ipAddress(), true);

        UserTwoFactorSettings twoFactorSettings = twoFactorService.getOrCreateSettings(user);
        if (twoFactorSettings.isEnabled()) {
            TwoFactorChallenge challenge = twoFactorService.createChallenge(
                user,
                TwoFactorChallengePurpose.LOGIN,
                twoFactorSettings.getMethod()
            );
            return LoginResult.twoFactorRequired(challenge.getId(), challenge.getMethod());
        }

        return LoginResult.authenticated(issueTokensForUser(user, command.ipAddress(), command.userAgent(), false));
    }

    @Override
    @Transactional
    public AuthTokens verifyTwoFactorLogin(TwoFactorVerifyCommand command) {
        requireNonBlank(command.code(), "Two-factor code is required.");
        TwoFactorChallenge challenge = twoFactorService.consumeChallenge(
            command.challengeId(),
            command.code(),
            TwoFactorChallengePurpose.LOGIN
        );
        AuthUser user = challenge.getUser();
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new UserDisabledException();
        }
        return issueTokensForUser(user, command.ipAddress(), command.userAgent(), true);
    }

    private AuthTokens issueTokensForUser(AuthUser user, String ipAddress, String userAgent, boolean twoFactorVerified) {
        sessionService.enforceLoginPolicy(user);

        Instant now = Instant.now(clock);
        AuthSession session = sessionService.createSession(
            user,
            ipAddress,
            userAgent,
            now.plus(authProperties.getRefreshTokenTtl()),
            twoFactorVerified
        );

        TokenPair tokenPair = tokenService.issueTokenPair(user.getId(), session.getId(), now, null);
        saveRefreshToken(session, tokenPair);
        return toAuthTokens(tokenPair);
    }

    @Override
    @Transactional
    public AuthTokens refresh(RefreshCommand command) {
        Instant now = Instant.now(clock);
        requireNonBlank(command.refreshToken(), "Refresh token is required.");
        String incomingRefreshToken = command.refreshToken();
        String refreshHash = tokenService.hashRefreshToken(incomingRefreshToken);

        RefreshToken existing = refreshTokenRepository.findByTokenHash(refreshHash)
            .orElseThrow(InvalidRefreshTokenException::new);

        if (existing.getRevokedAt() != null || existing.getExpiresAt().isBefore(now)) {
            handleRefreshTokenReuse(existing, now);
            throw new InvalidRefreshTokenException();
        }

        AuthSession session = existing.getSession();
        if (session.getRevokedAt() != null || session.getExpiresAt().isBefore(now)) {
            throw new InvalidRefreshTokenException();
        }

        AuthUser user = session.getUser();
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new UserDisabledException();
        }

        UUID familyId = existing.getTokenFamilyId();
        if (authProperties.isRefreshRotationEnabled()) {
            existing.setRevokedAt(now);
            refreshTokenRepository.save(existing);
        }

        TokenPair tokenPair = tokenService.issueTokenPair(user.getId(), session.getId(), now, familyId);
        RefreshToken replacement = saveRefreshToken(session, tokenPair);
        if (authProperties.isRefreshRotationEnabled()) {
            existing.setReplacedByTokenId(replacement.getId());
            refreshTokenRepository.save(existing);
        }

        return toAuthTokens(tokenPair);
    }

    private void handleRefreshTokenReuse(RefreshToken token, Instant now) {
        if (!authProperties.isRefreshReuseDetectionEnabled()) {
            return;
        }

        List<RefreshToken> familyTokens = refreshTokenRepository.findByTokenFamilyIdAndRevokedAtIsNull(
            token.getTokenFamilyId()
        );
        for (RefreshToken familyToken : familyTokens) {
            familyToken.setRevokedAt(now);
        }
        refreshTokenRepository.saveAll(familyTokens);
        sessionService.revokeSession(token.getSession(), "Revoked due to refresh token replay detection", now);
    }

    private RefreshToken saveRefreshToken(AuthSession session, TokenPair tokenPair) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setSession(session);
        refreshToken.setTokenHash(tokenPair.refreshTokenHash());
        refreshToken.setTokenFamilyId(tokenPair.tokenFamilyId());
        refreshToken.setExpiresAt(tokenPair.refreshExpiresAt());
        return refreshTokenRepository.save(refreshToken);
    }

    private AuthTokens toAuthTokens(TokenPair tokenPair) {
        return new AuthTokens(
            tokenPair.accessToken(),
            tokenPair.refreshToken(),
            tokenPair.accessExpiresAt(),
            tokenPair.refreshExpiresAt()
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void enforceLoginAttemptLimit(String email) {
        AuthPolicySettings policy = authPolicyService.getPolicy();
        Instant now = Instant.now(clock);
        Instant windowStart = now.minus(policy.getLoginAttemptWindow());
        long failedAttempts = loginAttemptService.countFailedAttemptsSince(email, windowStart);
        if (failedAttempts >= policy.getLoginAttemptLimit()) {
            long retryAfterSeconds = loginAttemptService.findOldestFailedAttemptSince(email, windowStart)
                .map(oldestAttempt -> Duration.between(now, oldestAttempt.plus(policy.getLoginAttemptWindow())).toSeconds())
                .orElse(policy.getLoginAttemptWindow().toSeconds());
            throw new LoginAttemptLimitExceededException(retryAfterSeconds);
        }
    }

    private void recordLoginAttempt(String email, String ipAddress, boolean successful) {
        loginAttemptService.recordAttempt(email, ipAddress, successful);
    }
}
