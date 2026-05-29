package id.ac.ui.cs.advprog.bidmart.auth.controller;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.AuthPolicyResponse;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.CreatePermissionRequest;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.CreateRoleRequest;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.PermissionResponse;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.RoleResponse;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.UpdateAuthPolicyRequest;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.UserResponse;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.UserStatusResponse;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPolicySettings;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserStatus;
import id.ac.ui.cs.advprog.bidmart.auth.service.AuthPolicyService;
import id.ac.ui.cs.advprog.bidmart.auth.service.PermissionService;
import id.ac.ui.cs.advprog.bidmart.auth.service.UserManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminAuthControllerTest {
    private PermissionService permissionService;
    private UserManagementService userManagementService;
    private AuthPolicyService authPolicyService;
    private AdminAuthController controller;

    @BeforeEach
    void setUp() {
        permissionService = mock(PermissionService.class);
        userManagementService = mock(UserManagementService.class);
        authPolicyService = mock(AuthPolicyService.class);
        controller = new AdminAuthController(permissionService, userManagementService, authPolicyService);
    }

    @Test
    void getPolicyMapsPolicySettings() {
        AuthPolicySettings policy = policy();
        when(authPolicyService.getPolicy()).thenReturn(policy);

        AuthPolicyResponse response = controller.getPolicy();

        assertEquals(3, response.maxConcurrentSessions());
        assertEquals("REVOKE_OLDEST", response.concurrentSessionPolicy());
        assertEquals(5, response.loginAttemptLimit());
        assertEquals(900, response.loginAttemptWindowSeconds());
        assertEquals(4, response.otpAttemptLimit());
        assertEquals(300, response.otpTtlSeconds());
    }

    @Test
    void updatePolicyUsesRequestDurations() {
        AuthPolicySettings policy = policy();
        UpdateAuthPolicyRequest request = new UpdateAuthPolicyRequest(
            2,
            AuthProperties.ConcurrentSessionPolicy.REJECT_NEW,
            7,
            600,
            3,
            120
        );
        when(authPolicyService.updatePolicy(
            2,
            AuthProperties.ConcurrentSessionPolicy.REJECT_NEW,
            7,
            Duration.ofSeconds(600),
            3,
            Duration.ofSeconds(120)
        )).thenReturn(policy);

        AuthPolicyResponse response = controller.updatePolicy(request);

        assertEquals("REVOKE_OLDEST", response.concurrentSessionPolicy());
        verify(authPolicyService).updatePolicy(
            2,
            AuthProperties.ConcurrentSessionPolicy.REJECT_NEW,
            7,
            Duration.ofSeconds(600),
            3,
            Duration.ofSeconds(120)
        );
    }

    @Test
    void listRolesMapsRoles() {
        AuthRole role = role(UUID.randomUUID(), "SELLER", "Can sell", true);
        when(permissionService.listRoles()).thenReturn(List.of(role));

        List<RoleResponse> response = controller.listRoles();

        assertEquals(1, response.size());
        assertEquals(role.getId(), response.getFirst().id());
        assertEquals("SELLER", response.getFirst().name());
        assertEquals("Can sell", response.getFirst().description());
        assertEquals(true, response.getFirst().systemRole());
    }

    @Test
    void createRoleReturnsCreatedResponse() {
        AuthRole role = role(UUID.randomUUID(), "MANAGER", "Can manage", false);
        when(permissionService.createRole("manager", "Can manage")).thenReturn(role);

        ResponseEntity<RoleResponse> response = controller.createRole(new CreateRoleRequest("manager", "Can manage"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("MANAGER", response.getBody().name());
    }

    @Test
    void rolePermissionEndpointsDelegateAndReturnNoContent() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();

        ResponseEntity<Void> assignResponse = controller.assignPermissionToRole(roleId, permissionId);
        ResponseEntity<Void> removeResponse = controller.removePermissionFromRole(roleId, permissionId);

        assertEquals(HttpStatus.NO_CONTENT, assignResponse.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, removeResponse.getStatusCode());
        verify(permissionService).assignPermissionToRole(roleId, permissionId);
        verify(permissionService).removePermissionFromRole(roleId, permissionId);
    }

    @Test
    void listPermissionsMapsPermissions() {
        AuthPermission permission = permission(UUID.randomUUID(), "auth:read", "Read auth");
        when(permissionService.listPermissions()).thenReturn(List.of(permission));

        List<PermissionResponse> response = controller.listPermissions();

        assertEquals(permission.getId(), response.getFirst().id());
        assertEquals("auth:read", response.getFirst().name());
        assertEquals("Read auth", response.getFirst().description());
    }

    @Test
    void createPermissionReturnsCreatedResponse() {
        AuthPermission permission = permission(UUID.randomUUID(), "auth:write", "Write auth");
        when(permissionService.createPermission("auth:write", "Write auth")).thenReturn(permission);

        ResponseEntity<PermissionResponse> response = controller.createPermission(
            new CreatePermissionRequest("auth:write", "Write auth")
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("auth:write", response.getBody().name());
    }

    @Test
    void listUsersMapsUsers() {
        AuthUser user = user(UUID.randomUUID(), "buyer@example.com", UserRole.BUYER, UserStatus.ACTIVE, null);
        when(userManagementService.listUsers()).thenReturn(List.of(user));

        List<UserResponse> response = controller.listUsers();

        assertEquals(user.getId(), response.getFirst().id());
        assertEquals("buyer@example.com", response.getFirst().email());
        assertEquals("BUYER", response.getFirst().primaryRole());
        assertEquals("ACTIVE", response.getFirst().status());
        assertNull(response.getFirst().disabledAt());
    }

    @Test
    void userRoleAndPermissionEndpointsDelegateAndReturnNoContent() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();

        assertEquals(HttpStatus.NO_CONTENT, controller.assignRoleToUser(userId, roleId).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.removeRoleFromUser(userId, roleId).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.assignPermissionToUser(userId, permissionId).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.removePermissionFromUser(userId, permissionId).getStatusCode());
        verify(permissionService).assignRoleToUser(userId, roleId);
        verify(permissionService).removeRoleFromUser(userId, roleId);
        verify(permissionService).assignPermissionToUser(userId, permissionId);
        verify(permissionService).removePermissionFromUser(userId, permissionId);
    }

    @Test
    void disableUserMapsStatusResponse() {
        UUID userId = UUID.randomUUID();
        Instant disabledAt = Instant.parse("2026-05-21T10:00:00Z");
        AuthUser user = user(userId, "buyer@example.com", UserRole.BUYER, UserStatus.DISABLED, disabledAt);
        when(userManagementService.disableUser(userId)).thenReturn(user);

        UserStatusResponse response = controller.disableUser(userId);

        assertEquals(userId, response.id());
        assertEquals("DISABLED", response.status());
        assertEquals(disabledAt, response.disabledAt());
    }

    private AuthPolicySettings policy() {
        AuthPolicySettings policy = new AuthPolicySettings();
        policy.setMaxConcurrentSessions(3);
        policy.setConcurrentSessionPolicy(AuthProperties.ConcurrentSessionPolicy.REVOKE_OLDEST);
        policy.setLoginAttemptLimit(5);
        policy.setLoginAttemptWindow(Duration.ofMinutes(15));
        policy.setOtpAttemptLimit(4);
        policy.setOtpTtl(Duration.ofMinutes(5));
        return policy;
    }

    private AuthRole role(UUID id, String name, String description, boolean systemRole) {
        AuthRole role = new AuthRole();
        ReflectionTestUtils.setField(role, "id", id);
        role.setName(name);
        role.setDescription(description);
        role.setSystemRole(systemRole);
        return role;
    }

    private AuthPermission permission(UUID id, String name, String description) {
        AuthPermission permission = new AuthPermission();
        ReflectionTestUtils.setField(permission, "id", id);
        permission.setName(name);
        permission.setDescription(description);
        return permission;
    }

    private AuthUser user(UUID id, String email, UserRole role, UserStatus status, Instant disabledAt) {
        AuthUser user = new AuthUser();
        ReflectionTestUtils.setField(user, "id", id);
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setPrimaryRole(role);
        user.setStatus(status);
        user.setDisabledAt(disabledAt);
        return user;
    }
}
