package id.ac.ui.cs.advprog.bidmart.auth.security;

import id.ac.ui.cs.advprog.bidmart.auth.exception.InvalidAccessTokenException;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthSession;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserStatus;
import id.ac.ui.cs.advprog.bidmart.auth.service.SessionService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TokenService;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.AccessTokenClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final SessionService sessionService;

    public JwtAuthenticationFilter(TokenService tokenService, SessionService sessionService) {
        this.tokenService = tokenService;
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authorization.substring(BEARER_PREFIX.length()).trim();
        try {
            AccessTokenClaims claims = tokenService.parseAccessToken(accessToken);
            authenticateIfValid(claims);
        } catch (InvalidAccessTokenException ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateIfValid(AccessTokenClaims claims) {
        Optional<AuthSession> activeSessionOpt = sessionService.findActiveById(claims.sessionId());
        if (activeSessionOpt.isEmpty()) {
            SecurityContextHolder.clearContext();
            return;
        }

        AuthSession activeSession = activeSessionOpt.get();
        if (activeSession.getUser().getStatus() == UserStatus.DISABLED) {
            SecurityContextHolder.clearContext();
            return;
        }

        UUID userIdFromSession = activeSession.getUser().getId();
        if (!userIdFromSession.equals(claims.userId())) {
            SecurityContextHolder.clearContext();
            return;
        }

        String roleName = activeSession.getUser().getPrimaryRole().name();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userIdFromSession.toString(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_" + roleName))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
