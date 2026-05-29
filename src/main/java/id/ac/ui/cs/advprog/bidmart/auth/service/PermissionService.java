package id.ac.ui.cs.advprog.bidmart.auth.service;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRole;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PermissionService {
    Set<String> resolvePermissions(UUID userId);

    boolean hasAnyAllowedAndNoForbidden(UUID userId, String[] allowed, String[] forbidden);

    AuthRole createRole(String name, String description);

    List<AuthRole> listRoles();

    AuthPermission createPermission(String name, String description);

    List<AuthPermission> listPermissions();

    void assignPermissionToRole(UUID roleId, UUID permissionId);

    void removePermissionFromRole(UUID roleId, UUID permissionId);

    void assignRoleToUser(UUID userId, UUID roleId);

    void removeRoleFromUser(UUID userId, UUID roleId);

    void assignPermissionToUser(UUID userId, UUID permissionId);

    void removePermissionFromUser(UUID userId, UUID permissionId);
}
