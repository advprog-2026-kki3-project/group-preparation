package id.ac.ui.cs.advprog.bidmart.auth.security;

import id.ac.ui.cs.advprog.bidmart.auth.exception.ForbiddenPermissionException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.TwoFactorRequiredException;
import id.ac.ui.cs.advprog.bidmart.auth.service.PermissionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PermissionInterceptorTest {
    private PermissionService permissionService;
    private PermissionInterceptor interceptor;
    private FixtureController fixture;
    private OpenController openController;

    @BeforeEach
    void setUp() {
        permissionService = mock(PermissionService.class);
        interceptor = new PermissionInterceptor(permissionService);
        fixture = new FixtureController();
        openController = new OpenController();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void preHandleAllowsNonHandlerMethod() {
        assertTrue(interceptor.preHandle(null, null, new Object()));

        verifyNoInteractions(permissionService);
    }

    @Test
    void preHandleAllowsHandlerWithoutPermissionRequirement() throws Exception {
        Method method = OpenController.class.getDeclaredMethod("openEndpoint");

        assertTrue(interceptor.preHandle(null, null, new HandlerMethod(openController, method)));

        verifyNoInteractions(permissionService);
    }

    @Test
    void preHandleRejectsMissingAuthentication() throws Exception {
        assertThrows(TwoFactorRequiredException.class, () -> interceptor.preHandle(null, null, handler("adminEndpoint")));
    }

    @Test
    void preHandleRejectsUnauthenticatedPrincipal() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
            UsernamePasswordAuthenticationToken.unauthenticated(UUID.randomUUID().toString(), null)
        );

        assertThrows(ForbiddenPermissionException.class, () -> interceptor.preHandle(null, null, handler("adminEndpoint")));
    }

    @Test
    void preHandleRejectsWhenTwoFactorRequiredButMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        var authentication = authenticated(userId);
        authentication.setDetails(new AuthRequestDetails(UUID.randomUUID(), false));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(ForbiddenPermissionException.class, () -> interceptor.preHandle(null, null, handler("adminEndpoint")));
    }

    @Test
    void preHandleAllowsMethodRequirementWhenPermissionMatches() throws Exception {
        UUID userId = UUID.randomUUID();
        var authentication = authenticated(userId);
        authentication.setDetails(new AuthRequestDetails(UUID.randomUUID(), true));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(permissionService.hasAnyAllowedAndNoForbidden(
            eq(userId),
            aryEq(new String[] {"auth:admin"}),
            aryEq(new String[] {"auth:banned"})
        )).thenReturn(true);

        assertTrue(interceptor.preHandle(null, null, handler("adminEndpoint")));
    }

    @Test
    void preHandleRejectsWhenPermissionServiceDenies() throws Exception {
        UUID userId = UUID.randomUUID();
        var authentication = authenticated(userId);
        authentication.setDetails(new AuthRequestDetails(UUID.randomUUID(), true));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(permissionService.hasAnyAllowedAndNoForbidden(any(), aryEq(new String[] {"auth:admin"}), aryEq(new String[] {"auth:banned"})))
            .thenReturn(false);

        assertThrows(ForbiddenPermissionException.class, () -> interceptor.preHandle(null, null, handler("adminEndpoint")));
    }

    @Test
    void preHandleUsesClassLevelRequirementWhenMethodHasNoAnnotation() throws Exception {
        UUID userId = UUID.randomUUID();
        var authentication = authenticated(userId);
        authentication.setDetails("not-auth-details");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(permissionService.hasAnyAllowedAndNoForbidden(
            eq(userId),
            aryEq(new String[] {"auth:class"}),
            aryEq(new String[0])
        ))
            .thenReturn(true);

        assertTrue(interceptor.preHandle(null, null, handler("classLevelEndpoint")));
    }

    private UsernamePasswordAuthenticationToken authenticated(UUID userId) {
        return UsernamePasswordAuthenticationToken.authenticated(userId.toString(), null, java.util.List.of());
    }

    private HandlerMethod handler(String methodName) throws NoSuchMethodException {
        Method method = FixtureController.class.getDeclaredMethod(methodName);
        return new HandlerMethod(fixture, method);
    }

    @RequiresPermission(allowed = "auth:class", requireTwoFactor = false)
    private static final class FixtureController {
        void classLevelEndpoint() {
        }

        @RequiresPermission(allowed = "auth:admin", forbidden = "auth:banned")
        void adminEndpoint() {
        }
    }

    private static final class OpenController {
        void openEndpoint() {
        }
    }
}
