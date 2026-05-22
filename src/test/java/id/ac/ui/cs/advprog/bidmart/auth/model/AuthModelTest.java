package id.ac.ui.cs.advprog.bidmart.auth.model;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthModelTest {

    @Test
    void authUserNormalizesEmailOnCreateAndUpdate() {
        AuthUser user = new AuthUser();
        user.setEmail("  User@Example.COM ");
        user.setPasswordHash("hash");
        user.setPrimaryRole(UserRole.BUYER);
        user.setStatus(UserStatus.DISABLED);
        Instant disabledAt = Instant.parse("2026-05-21T10:15:30Z");
        user.setDisabledAt(disabledAt);

        user.onCreate();

        assertEquals("user@example.com", user.getEmail());
        assertEquals("hash", user.getPasswordHash());
        assertEquals(UserRole.BUYER, user.getPrimaryRole());
        assertEquals(UserStatus.DISABLED, user.getStatus());
        assertEquals(disabledAt, user.getDisabledAt());
        assertNull(user.getId());
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());

        user.setEmail("  Updated@Example.COM ");
        user.onUpdate();

        assertEquals("updated@example.com", user.getEmail());
    }

    @Test
    void authUserLeavesNullEmailWhenNormalizing() {
        AuthUser user = new AuthUser();

        user.onCreate();
        user.onUpdate();

        assertNull(user.getEmail());
    }

    @Test
    void authPermissionAndRoleNormalizeNames() {
        AuthPermission permission = new AuthPermission();
        permission.setName("  USER:READ ");
        permission.setDescription("Read users");

        permission.normalize();

        assertEquals("user:read", permission.getName());
        assertEquals("Read users", permission.getDescription());
        assertNull(permission.getId());

        AuthRole role = new AuthRole();
        role.setName(" admin ");
        role.setDescription("Admin role");
        role.setSystemRole(true);

        role.normalize();

        assertEquals("ADMIN", role.getName());
        assertEquals("Admin role", role.getDescription());
        assertTrue(role.isSystemRole());
        assertNull(role.getId());
    }

    @Test
    void authPermissionAndRoleLeaveNullNameWhenNormalizing() {
        AuthPermission permission = new AuthPermission();
        AuthRole role = new AuthRole();

        permission.normalize();
        role.normalize();

        assertNull(permission.getName());
        assertNull(role.getName());
    }

    @Test
    void loginAttemptNormalizesEmailOnlyWhenPresent() {
        LoginAttempt attempt = new LoginAttempt();
        Instant attemptedAt = Instant.parse("2026-05-21T11:00:00Z");
        attempt.setEmail("  Buyer@Example.COM ");
        attempt.setIpAddress("127.0.0.1");
        attempt.setSuccessful(true);
        attempt.setAttemptedAt(attemptedAt);

        attempt.onCreate();

        assertEquals(attemptedAt, attempt.getAttemptedAt());
        assertEquals("buyer@example.com", ReflectionTestUtils.getField(attempt, "email"));
        assertEquals("127.0.0.1", ReflectionTestUtils.getField(attempt, "ipAddress"));
        assertEquals(true, ReflectionTestUtils.getField(attempt, "successful"));

        LoginAttempt nullEmailAttempt = new LoginAttempt();
        nullEmailAttempt.onCreate();

        assertNull(ReflectionTestUtils.getField(nullEmailAttempt, "email"));
    }

    @Test
    void authUserRoleSetsAssignedAtOnlyWhenMissing() {
        assertNull(new AuthUserRole().getUserId());

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        AuthUserRole role = new AuthUserRole(userId, roleId);

        role.onCreate();

        assertEquals(userId, role.getUserId());
        assertEquals(roleId, role.getRoleId());
        assertNotNull(ReflectionTestUtils.getField(role, "assignedAt"));

        Instant assignedAt = Instant.parse("2026-05-21T12:00:00Z");
        ReflectionTestUtils.setField(role, "assignedAt", assignedAt);

        role.onCreate();

        assertEquals(assignedAt, ReflectionTestUtils.getField(role, "assignedAt"));
    }

    @Test
    void authUserPermissionSetsAssignedAtOnlyWhenMissing() {
        assertNull(ReflectionTestUtils.getField(new AuthUserPermission(), "userId"));

        UUID userId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        AuthUserPermission permission = new AuthUserPermission(userId, permissionId);

        permission.onCreate();

        assertEquals(userId, ReflectionTestUtils.getField(permission, "userId"));
        assertEquals(permissionId, ReflectionTestUtils.getField(permission, "permissionId"));
        assertNotNull(ReflectionTestUtils.getField(permission, "assignedAt"));

        Instant assignedAt = Instant.parse("2026-05-21T13:00:00Z");
        ReflectionTestUtils.setField(permission, "assignedAt", assignedAt);

        permission.onCreate();

        assertEquals(assignedAt, ReflectionTestUtils.getField(permission, "assignedAt"));
    }

    @Test
    void authPolicySettingsExposeDurationsAndValues() {
        AuthPolicySettings settings = new AuthPolicySettings();
        settings.setMaxConcurrentSessions(3);
        settings.setConcurrentSessionPolicy(AuthProperties.ConcurrentSessionPolicy.REVOKE_OLDEST);
        settings.setLoginAttemptLimit(5);
        settings.setLoginAttemptWindow(Duration.ofMinutes(10));
        settings.setOtpAttemptLimit(4);
        settings.setOtpTtl(Duration.ofSeconds(90));

        assertEquals(AuthPolicySettings.SINGLETON_ID, settings.getId());
        assertEquals(3, settings.getMaxConcurrentSessions());
        assertEquals(AuthProperties.ConcurrentSessionPolicy.REVOKE_OLDEST, settings.getConcurrentSessionPolicy());
        assertEquals(5, settings.getLoginAttemptLimit());
        assertEquals(Duration.ofMinutes(10), settings.getLoginAttemptWindow());
        assertEquals(4, settings.getOtpAttemptLimit());
        assertEquals(Duration.ofSeconds(90), settings.getOtpTtl());
        assertNull(settings.getUpdatedAt());
    }

    @Test
    void refreshTokenExposesMutableFields() {
        RefreshToken token = new RefreshToken();
        AuthSession session = new AuthSession();
        UUID familyId = UUID.randomUUID();
        UUID replacementId = UUID.randomUUID();
        Instant expiresAt = Instant.parse("2026-05-22T00:00:00Z");
        Instant revokedAt = Instant.parse("2026-05-21T14:00:00Z");

        token.setSession(session);
        token.setTokenHash("hash");
        token.setTokenFamilyId(familyId);
        token.setExpiresAt(expiresAt);
        token.setRevokedAt(revokedAt);
        token.setReplacedByTokenId(replacementId);

        assertNull(token.getId());
        assertSame(session, token.getSession());
        assertEquals("hash", token.getTokenHash());
        assertEquals(familyId, token.getTokenFamilyId());
        assertNull(token.getIssuedAt());
        assertEquals(expiresAt, token.getExpiresAt());
        assertEquals(revokedAt, token.getRevokedAt());
        assertEquals(replacementId, token.getReplacedByTokenId());
    }

    @Test
    void userTwoFactorSettingsExposeMutableFields() {
        UserTwoFactorSettings settings = new UserTwoFactorSettings();
        AuthUser user = new AuthUser();

        settings.setUser(user);
        settings.setEnabled(true);
        settings.setMethod(TwoFactorMethod.EMAIL_OTP);
        settings.setPendingMethod(TwoFactorMethod.TOTP);
        settings.setTotpSecret("current");
        settings.setPendingTotpSecret("pending");

        assertNull(settings.getUserId());
        assertSame(user, settings.getUser());
        assertTrue(settings.isEnabled());
        assertEquals(TwoFactorMethod.EMAIL_OTP, settings.getMethod());
        assertEquals(TwoFactorMethod.TOTP, settings.getPendingMethod());
        assertEquals("current", settings.getTotpSecret());
        assertEquals("pending", settings.getPendingTotpSecret());
    }

    @Test
    void compositeIdsCompareTypeFieldsAndHashCode() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        UUID different = UUID.randomUUID();

        assertCompositeIdBehavior(
            new AuthUserRoleId(first, second),
            new AuthUserRoleId(first, second),
            new AuthUserRoleId(first, different),
            new AuthUserRoleId(different, second)
        );
        assertCompositeIdBehavior(
            new AuthRolePermissionId(first, second),
            new AuthRolePermissionId(first, second),
            new AuthRolePermissionId(first, different),
            new AuthRolePermissionId(different, second)
        );
        assertCompositeIdBehavior(
            new AuthUserPermissionId(first, second),
            new AuthUserPermissionId(first, second),
            new AuthUserPermissionId(first, different),
            new AuthUserPermissionId(different, second)
        );
    }

    private static void assertCompositeIdBehavior(Object id, Object same, Object differentSecond, Object differentFirst) {
        assertEquals(id, id);
        assertEquals(id, same);
        assertEquals(id.hashCode(), same.hashCode());
        assertNotEquals(id, differentSecond);
        assertNotEquals(id, differentFirst);
        assertNotEquals(id, null);
        assertNotEquals(id, "other");
    }

    @Test
    void compositeIdsSupportNoArgConstruction() {
        assertEquals(new AuthUserRoleId(), new AuthUserRoleId());
        assertEquals(new AuthRolePermissionId(), new AuthRolePermissionId());
        assertEquals(new AuthUserPermissionId(), new AuthUserPermissionId());
        assertFalse(new AuthUserRoleId().equals(new AuthUserPermissionId()));
    }
}
