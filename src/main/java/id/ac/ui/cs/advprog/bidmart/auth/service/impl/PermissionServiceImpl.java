package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.exception.PermissionAlreadyExistsException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.ResourceNotFoundException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.RoleAlreadyExistsException;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRolePermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRolePermissionId;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUserPermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUserPermissionId;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUserRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUserRoleId;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserRole;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthPermissionRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthRolePermissionRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthRoleRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthUserPermissionRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthUserRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthUserRoleRepository;
import id.ac.ui.cs.advprog.bidmart.auth.service.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class PermissionServiceImpl implements PermissionService {
    private final AuthPermissionRepository permissionRepository;
    private final AuthRoleRepository roleRepository;
    private final AuthUserRepository userRepository;
    private final AuthRolePermissionRepository rolePermissionRepository;
    private final AuthUserRoleRepository userRoleRepository;
    private final AuthUserPermissionRepository userPermissionRepository;

    public PermissionServiceImpl(
        AuthPermissionRepository permissionRepository,
        AuthRoleRepository roleRepository,
        AuthUserRepository userRepository,
        AuthRolePermissionRepository rolePermissionRepository,
        AuthUserRoleRepository userRoleRepository,
        AuthUserPermissionRepository userPermissionRepository
    ) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.userPermissionRepository = userPermissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> resolvePermissions(UUID userId) {
        Set<String> permissions = new HashSet<>();
        permissions.addAll(permissionRepository.findDirectPermissionNamesByUserId(userId));
        permissions.addAll(permissionRepository.findRolePermissionNamesByUserId(userId));
        userRepository.findById(userId)
            .filter(user -> user.getPrimaryRole() == UserRole.ADMINISTRATOR)
            .ifPresent(user -> permissions.add("auth:admin"));
        return permissions;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAnyAllowedAndNoForbidden(UUID userId, String[] allowed, String[] forbidden) {
        Set<String> permissions = resolvePermissions(userId);
        for (String forbiddenPermission : forbidden) {
            if (permissions.contains(normalizePermission(forbiddenPermission))) {
                return false;
            }
        }
        if (allowed.length == 0) {
            return true;
        }
        for (String allowedPermission : allowed) {
            if (permissions.contains(normalizePermission(allowedPermission))) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public AuthRole createRole(String name, String description) {
        String normalized = normalizeRole(name);
        if (roleRepository.existsByNameIgnoreCase(normalized)) {
            throw new RoleAlreadyExistsException();
        }
        AuthRole role = new AuthRole();
        role.setName(normalized);
        role.setDescription(description);
        role.setSystemRole(false);
        return roleRepository.save(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthRole> listRoles() {
        return roleRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional
    public AuthPermission createPermission(String name, String description) {
        String normalized = normalizePermission(name);
        if (permissionRepository.existsByNameIgnoreCase(normalized)) {
            throw new PermissionAlreadyExistsException();
        }
        AuthPermission permission = new AuthPermission();
        permission.setName(normalized);
        permission.setDescription(description);
        return permissionRepository.save(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthPermission> listPermissions() {
        return permissionRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional
    public void assignPermissionToRole(UUID roleId, UUID permissionId) {
        requireRole(roleId);
        requirePermission(permissionId);
        AuthRolePermissionId id = new AuthRolePermissionId(roleId, permissionId);
        if (!rolePermissionRepository.existsById(id)) {
            rolePermissionRepository.save(new AuthRolePermission(roleId, permissionId));
        }
    }

    @Override
    @Transactional
    public void removePermissionFromRole(UUID roleId, UUID permissionId) {
        rolePermissionRepository.deleteByRoleIdAndPermissionId(roleId, permissionId);
    }

    @Override
    @Transactional
    public void assignRoleToUser(UUID userId, UUID roleId) {
        requireUser(userId);
        requireRole(roleId);
        AuthUserRoleId id = new AuthUserRoleId(userId, roleId);
        if (!userRoleRepository.existsById(id)) {
            userRoleRepository.save(new AuthUserRole(userId, roleId));
        }
    }

    @Override
    @Transactional
    public void removeRoleFromUser(UUID userId, UUID roleId) {
        userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
    }

    @Override
    @Transactional
    public void assignPermissionToUser(UUID userId, UUID permissionId) {
        requireUser(userId);
        requirePermission(permissionId);
        AuthUserPermissionId id = new AuthUserPermissionId(userId, permissionId);
        if (!userPermissionRepository.existsById(id)) {
            userPermissionRepository.save(new AuthUserPermission(userId, permissionId));
        }
    }

    @Override
    @Transactional
    public void removePermissionFromUser(UUID userId, UUID permissionId) {
        userPermissionRepository.deleteByUserIdAndPermissionId(userId, permissionId);
    }

    private void requireUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found.");
        }
    }

    private void requireRole(UUID roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new ResourceNotFoundException("Role not found.");
        }
    }

    private void requirePermission(UUID permissionId) {
        if (!permissionRepository.existsById(permissionId)) {
            throw new ResourceNotFoundException("Permission not found.");
        }
    }

    private String normalizeRole(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role name is required.");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizePermission(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Permission name is required.");
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
