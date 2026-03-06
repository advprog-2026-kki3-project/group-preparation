package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import id.ac.ui.cs.advprog.bidmart.auth.exception.EmailAlreadyUsedException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.InvalidCredentialsException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.InvalidRefreshTokenException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.UserDisabledException;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthSession;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.RefreshToken;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserStatus;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthUserRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.RefreshTokenRepository;
import id.ac.ui.cs.advprog.bidmart.auth.service.AuthenticationService;
import id.ac.ui.cs.advprog.bidmart.auth.service.CredentialService;
import id.ac.ui.cs.advprog.bidmart.auth.service.SessionService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TokenService;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.AuthTokens;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.LoginCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.RefreshCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.RegisterCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.TokenPair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthUserRepository authUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CredentialService credentialService;
    private final SessionService sessionService;
    private final TokenService tokenService;
    private final AuthProperties authProperties;
    private final Clock clock;

    public AuthenticationServiceImpl(
        AuthUserRepository authUserRepository,
        RefreshTokenRepository refreshTokenRepository,
        CredentialService credentialService,
        SessionService sessionService,
        TokenService tokenService,
        AuthProperties authProperties,
        Clock clock
    ) {
        this.authUserRepository = authUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.credentialService = credentialService;
        this.sessionService = sessionService;
        this.tokenService = tokenService;
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
        if (requestedRole == UserRole.ADMINISTRATOR) {
            requestedRole = UserRole.BUYER;
        }

        AuthUser user = new AuthUser();
        user.setEmail(email);
        user.setPasswordHash(credentialService.hashPassword(command.rawPassword()));
        user.setPrimaryRole(requestedRole);
        user.setStatus(UserStatus.ACTIVE);
        authUserRepository.save(user);
    }

    @Override
    @Transactional
    public AuthTokens login(LoginCommand command) {
        requireNonBlank(command.email(), "Email is required.");
        requireNonBlank(command.rawPassword(), "Password is required.");

        AuthUser user = authUserRepository.findByEmailIgnoreCase(normalizeEmail(command.email()))
            .orElseThrow(InvalidCredentialsException::new);

        if (user.getStatus() == UserStatus.DISABLED) {
            throw new UserDisabledException();
        }

        if (!credentialService.matches(command.rawPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        sessionService.enforceLoginPolicy(user);

        Instant now = Instant.now(clock);
        AuthSession session = sessionService.createSession(
            user,
            command.ipAddress(),
            command.userAgent(),
            now.plus(authProperties.getRefreshTokenTtl())
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
}
