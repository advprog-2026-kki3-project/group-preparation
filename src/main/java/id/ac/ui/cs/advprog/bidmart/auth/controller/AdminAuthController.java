package id.ac.ui.cs.advprog.bidmart.auth.controller;

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
import id.ac.ui.cs.advprog.bidmart.auth.security.RequiresPermission;
import id.ac.ui.cs.advprog.bidmart.auth.service.AuthPolicyService;
import id.ac.ui.cs.advprog.bidmart.auth.service.PermissionService;
import id.ac.ui.cs.advprog.bidmart.auth.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/admin")
@RequiresPermission(allowed = "auth:admin")
public class AdminAuthController {
    private final PermissionService permissionService;
    private final UserManagementService userManagementService;
    private final AuthPolicyService authPolicyService;

    public AdminAuthController(
        PermissionService permissionService,
        UserManagementService userManagementService,
        AuthPolicyService authPolicyService
    ) {
        this.permissionService = permissionService;
        this.userManagementService = userManagementService;
        this.authPolicyService = authPolicyService;
    }

    @GetMapping("/policy")
    public AuthPolicyResponse getPolicy() {
        return toPolicyResponse(authPolicyService.getPolicy());
    }

    @PutMapping("/policy")
    public AuthPolicyResponse updatePolicy(@Valid @RequestBody UpdateAuthPolicyRequest request) {
        return toPolicyResponse(authPolicyService.updatePolicy(
            request.maxConcurrentSessions(),
            request.concurrentSessionPolicy(),
            request.loginAttemptLimit(),
            Duration.ofSeconds(request.loginAttemptWindowSeconds()),
            request.otpAttemptLimit(),
            Duration.ofSeconds(request.otpTtlSeconds())
        ));
    }

    @GetMapping("/roles")
    public List<RoleResponse> listRoles() {
        return permissionService.listRoles().stream().map(this::toRoleResponse).toList();
    }

    @PostMapping("/roles")
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        AuthRole role = permissionService.createRole(request.name(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(toRoleResponse(role));
    }

    @PostMapping("/roles/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Void> assignPermissionToRole(
        @PathVariable UUID roleId,
        @PathVariable UUID permissionId
    ) {
        permissionService.assignPermissionToRole(roleId, permissionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Void> removePermissionFromRole(
        @PathVariable UUID roleId,
        @PathVariable UUID permissionId
    ) {
        permissionService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/permissions")
    public List<PermissionResponse> listPermissions() {
        return permissionService.listPermissions().stream().map(this::toPermissionResponse).toList();
    }

    @GetMapping("/users")
    public List<UserResponse> listUsers() {
        return userManagementService.listUsers().stream().map(this::toUserResponse).toList();
    }

    @PostMapping("/permissions")
    public ResponseEntity<PermissionResponse> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        AuthPermission permission = permissionService.createPermission(request.name(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(toPermissionResponse(permission));
    }

    @PostMapping("/users/{userId}/roles/{roleId}")
    public ResponseEntity<Void> assignRoleToUser(@PathVariable UUID userId, @PathVariable UUID roleId) {
        permissionService.assignRoleToUser(userId, roleId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    public ResponseEntity<Void> removeRoleFromUser(@PathVariable UUID userId, @PathVariable UUID roleId) {
        permissionService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/permissions/{permissionId}")
    public ResponseEntity<Void> assignPermissionToUser(@PathVariable UUID userId, @PathVariable UUID permissionId) {
        permissionService.assignPermissionToUser(userId, permissionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{userId}/permissions/{permissionId}")
    public ResponseEntity<Void> removePermissionFromUser(@PathVariable UUID userId, @PathVariable UUID permissionId) {
        permissionService.removePermissionFromUser(userId, permissionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/disable")
    public UserStatusResponse disableUser(@PathVariable UUID userId) {
        AuthUser user = userManagementService.disableUser(userId);
        return new UserStatusResponse(user.getId(), user.getStatus().name(), user.getDisabledAt());
    }

    private RoleResponse toRoleResponse(AuthRole role) {
        return new RoleResponse(role.getId(), role.getName(), role.getDescription(), role.isSystemRole());
    }

    private PermissionResponse toPermissionResponse(AuthPermission permission) {
        return new PermissionResponse(permission.getId(), permission.getName(), permission.getDescription());
    }

    private UserResponse toUserResponse(AuthUser user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getPrimaryRole().name(),
            user.getStatus().name(),
            user.getDisabledAt()
        );
    }

    private AuthPolicyResponse toPolicyResponse(AuthPolicySettings settings) {
        return new AuthPolicyResponse(
            settings.getMaxConcurrentSessions(),
            settings.getConcurrentSessionPolicy().name(),
            settings.getLoginAttemptLimit(),
            settings.getLoginAttemptWindow().toSeconds(),
            settings.getOtpAttemptLimit(),
            settings.getOtpTtl().toSeconds(),
            settings.getUpdatedAt()
        );
    }
}
