package id.ac.ui.cs.advprog.bidmart.auth.security;

import id.ac.ui.cs.advprog.bidmart.auth.exception.InvalidAccessTokenException;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthSession;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserStatus;
import id.ac.ui.cs.advprog.bidmart.auth.service.PermissionService;
import id.ac.ui.cs.advprog.bidmart.auth.service.SessionService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TokenService;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.AccessTokenClaims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {
    private TokenService tokenService;
    private SessionService sessionService;
    private PermissionService permissionService;
    private JwtAuthenticationFilter filter;
    private FilterChain filterChain;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        tokenService = mock(TokenService.class);
        sessionService = mock(SessionService.class);
        permissionService = mock(PermissionService.class);
        filter = new JwtAuthenticationFilter(tokenService, sessionService, permissionService);
        filterChain = mock(FilterChain.class);
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterBypassesOptionsRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/me");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(tokenService, sessionService, permissionService);
    }

    @Test
    void doFilterBypassesMissingBearerToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(tokenService, sessionService, permissionService);
    }

    @Test
    void doFilterClearsContextWhenTokenIsInvalid() throws Exception {
        MockHttpServletRequest request = bearerRequest("bad-token");
        when(tokenService.parseAccessToken("bad-token")).thenThrow(new InvalidAccessTokenException());

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterClearsContextWhenSessionMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        MockHttpServletRequest request = bearerRequest("access-token");
        when(tokenService.parseAccessToken("access-token"))
            .thenReturn(new AccessTokenClaims(userId, sessionId, Instant.now().plusSeconds(60)));
        when(sessionService.findActiveById(sessionId)).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterClearsContextWhenUserIsDisabled() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        MockHttpServletRequest request = bearerRequest("access-token");
        when(tokenService.parseAccessToken("access-token"))
            .thenReturn(new AccessTokenClaims(userId, sessionId, Instant.now().plusSeconds(60)));
        when(sessionService.findActiveById(sessionId)).thenReturn(Optional.of(session(userId, sessionId, UserStatus.DISABLED)));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterClearsContextWhenClaimUserDoesNotMatchSessionUser() throws Exception {
        UUID sessionUserId = UUID.randomUUID();
        UUID claimedUserId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        MockHttpServletRequest request = bearerRequest("access-token");
        when(tokenService.parseAccessToken("access-token"))
            .thenReturn(new AccessTokenClaims(claimedUserId, sessionId, Instant.now().plusSeconds(60)));
        when(sessionService.findActiveById(sessionId)).thenReturn(Optional.of(session(sessionUserId, sessionId, UserStatus.ACTIVE)));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterSetsAuthenticationForValidTokenAndSession() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        MockHttpServletRequest request = bearerRequest("access-token");
        when(tokenService.parseAccessToken("access-token"))
            .thenReturn(new AccessTokenClaims(userId, sessionId, Instant.now().plusSeconds(60)));
        when(sessionService.findActiveById(sessionId)).thenReturn(Optional.of(session(userId, sessionId, UserStatus.ACTIVE)));
        when(permissionService.resolvePermissions(userId)).thenReturn(Set.of("auth:read"));

        filter.doFilterInternal(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(userId.toString(), authentication.getName());
        assertTrue(authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_BUYER")));
        assertTrue(authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("auth:read")));
        AuthRequestDetails details = assertInstanceOf(AuthRequestDetails.class, authentication.getDetails());
        assertEquals(sessionId, details.sessionId());
        assertTrue(details.twoFactorVerified());
        verify(filterChain).doFilter(request, response);
    }

    private MockHttpServletRequest bearerRequest(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private AuthSession session(UUID userId, UUID sessionId, UserStatus status) {
        AuthUser user = new AuthUser();
        ReflectionTestUtils.setField(user, "id", userId);
        user.setPrimaryRole(UserRole.BUYER);
        user.setStatus(status);
        AuthSession session = new AuthSession();
        ReflectionTestUtils.setField(session, "id", sessionId);
        session.setUser(user);
        session.setTwoFactorVerified(true);
        session.setExpiresAt(Instant.now().plusSeconds(60));
        return session;
    }
}
