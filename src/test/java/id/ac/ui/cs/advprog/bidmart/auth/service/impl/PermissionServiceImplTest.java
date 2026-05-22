package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.exception.PermissionAlreadyExistsException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.ResourceNotFoundException;
import id.ac.ui.cs.advprog.bidmart.auth.exception.RoleAlreadyExistsException;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRolePermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRolePermissionId;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {
    @Mock
    private AuthPermissionRepository permissionRepository;

    @Mock
    private AuthRoleRepository roleRepository;

    @Mock
    private AuthUserRepository userRepository;

    @Mock
    private AuthRolePermissionRepository rolePermissionRepository;

    @Mock
    private AuthUserRoleRepository userRoleRepository;

    @Mock
    private AuthUserPermissionRepository userPermissionRepository;

    private PermissionServiceImpl permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionServiceImpl(
            permissionRepository,
            roleRepository,
            userRepository,
            rolePermissionRepository,
            userRoleRepository,
            userPermissionRepository
        );
    }

    @Test
    void resolvePermissionsAddsAdminPermissionForAdministrator() {
        UUID userId = UUID.randomUUID();
        AuthUser user = new AuthUser();
        user.setPrimaryRole(UserRole.ADMINISTRATOR);
        when(permissionRepository.findDirectPermissionNamesByUserId(userId)).thenReturn(List.of("catalogue:create"));
        when(permissionRepository.findRolePermissionNamesByUserId(userId)).thenReturn(List.of("auction:manage"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Set<String> permissions = permissionService.resolvePermissions(userId);

        assertEquals(Set.of("catalogue:create", "auction:manage", "auth:admin"), permissions);
    }

    @Test
    void hasAnyAllowedAndNoForbiddenRejectsForbiddenPermission() {
        UUID userId = UUID.randomUUID();
        when(permissionRepository.findDirectPermissionNamesByUserId(userId)).thenReturn(List.of("auth:block"));
        when(permissionRepository.findRolePermissionNamesByUserId(userId)).thenReturn(List.of());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        boolean result = permissionService.hasAnyAllowedAndNoForbidden(
            userId,
            new String[] {"auth:read"},
            new String[] {" AUTH:BLOCK "}
        );

        assertFalse(result);
    }

    @Test
    void hasAnyAllowedAndNoForbiddenAcceptsMatchingAllowedPermission() {
        UUID userId = UUID.randomUUID();
        when(permissionRepository.findDirectPermissionNamesByUserId(userId)).thenReturn(List.of());
        when(permissionRepository.findRolePermissionNamesByUserId(userId)).thenReturn(List.of("auth:read"));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        boolean result = permissionService.hasAnyAllowedAndNoForbidden(
            userId,
            new String[] {" AUTH:READ "},
            new String[0]
        );

        assertTrue(result);
    }

    @Test
    void hasAnyAllowedAndNoForbiddenRejectsWhenAllowedPermissionMissing() {
        UUID userId = UUID.randomUUID();
        when(permissionRepository.findDirectPermissionNamesByUserId(userId)).thenReturn(List.of("auth:read"));
        when(permissionRepository.findRolePermissionNamesByUserId(userId)).thenReturn(List.of());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        boolean result = permissionService.hasAnyAllowedAndNoForbidden(
            userId,
            new String[] {"auth:write"},
            new String[0]
        );

        assertFalse(result);
    }

    @Test
    void createRoleNormalizesAndSavesRole() {
        ArgumentCaptor<AuthRole> captor = ArgumentCaptor.forClass(AuthRole.class);
        when(roleRepository.existsByNameIgnoreCase("SELLER")).thenReturn(false);
        when(roleRepository.save(any(AuthRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthRole role = permissionService.createRole(" seller ", "Can sell items");

        verify(roleRepository).save(captor.capture());
        assertSame(captor.getValue(), role);
        assertEquals("SELLER", role.getName());
        assertEquals("Can sell items", role.getDescription());
        assertFalse(role.isSystemRole());
    }

    @Test
    void createRoleRejectsDuplicateRole() {
        when(roleRepository.existsByNameIgnoreCase("SELLER")).thenReturn(true);

        assertThrows(RoleAlreadyExistsException.class, () -> permissionService.createRole(" seller ", "Duplicate"));

        verify(roleRepository, never()).save(any(AuthRole.class));
    }

    @Test
    void createRoleRejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> permissionService.createRole(" ", "Blank"));

        verifyNoInteractions(roleRepository);
    }

    @Test
    void listRolesDelegatesToRepository() {
        List<AuthRole> roles = List.of(new AuthRole(), new AuthRole());
        when(roleRepository.findAllByOrderByNameAsc()).thenReturn(roles);

        assertSame(roles, permissionService.listRoles());
    }

    @Test
    void createPermissionNormalizesAndSavesPermission() {
        ArgumentCaptor<AuthPermission> captor = ArgumentCaptor.forClass(AuthPermission.class);
        when(permissionRepository.existsByNameIgnoreCase("auth:write")).thenReturn(false);
        when(permissionRepository.save(any(AuthPermission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthPermission permission = permissionService.createPermission(" AUTH:WRITE ", "Can write auth data");

        verify(permissionRepository).save(captor.capture());
        assertSame(captor.getValue(), permission);
        assertEquals("auth:write", permission.getName());
        assertEquals("Can write auth data", permission.getDescription());
    }

    @Test
    void createPermissionRejectsDuplicatePermission() {
        when(permissionRepository.existsByNameIgnoreCase("auth:write")).thenReturn(true);

        assertThrows(
            PermissionAlreadyExistsException.class,
            () -> permissionService.createPermission(" AUTH:WRITE ", "Duplicate")
        );

        verify(permissionRepository, never()).save(any(AuthPermission.class));
    }

    @Test
    void createPermissionRejectsNullName() {
        assertThrows(IllegalArgumentException.class, () -> permissionService.createPermission(null, "Missing"));

        verifyNoInteractions(permissionRepository);
    }

    @Test
    void listPermissionsDelegatesToRepository() {
        List<AuthPermission> permissions = List.of(new AuthPermission(), new AuthPermission());
        when(permissionRepository.findAllByOrderByNameAsc()).thenReturn(permissions);

        assertSame(permissions, permissionService.listPermissions());
    }

    @Test
    void assignPermissionToRoleSavesNewAssignment() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        AuthRolePermissionId assignmentId = new AuthRolePermissionId(roleId, permissionId);
        when(roleRepository.existsById(roleId)).thenReturn(true);
        when(permissionRepository.existsById(permissionId)).thenReturn(true);
        when(rolePermissionRepository.existsById(assignmentId)).thenReturn(false);

        permissionService.assignPermissionToRole(roleId, permissionId);

        verify(rolePermissionRepository).save(any(AuthRolePermission.class));
    }

    @Test
    void assignPermissionToRoleSkipsExistingAssignment() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        AuthRolePermissionId assignmentId = new AuthRolePermissionId(roleId, permissionId);
        when(roleRepository.existsById(roleId)).thenReturn(true);
        when(permissionRepository.existsById(permissionId)).thenReturn(true);
        when(rolePermissionRepository.existsById(assignmentId)).thenReturn(true);

        permissionService.assignPermissionToRole(roleId, permissionId);

        verify(rolePermissionRepository, never()).save(any(AuthRolePermission.class));
    }

    @Test
    void assignPermissionToRoleRejectsMissingRole() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        when(roleRepository.existsById(roleId)).thenReturn(false);

        assertThrows(
            ResourceNotFoundException.class,
            () -> permissionService.assignPermissionToRole(roleId, permissionId)
        );

        verifyNoInteractions(permissionRepository, rolePermissionRepository);
    }

    @Test
    void assignPermissionToRoleRejectsMissingPermission() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        when(roleRepository.existsById(roleId)).thenReturn(true);
        when(permissionRepository.existsById(permissionId)).thenReturn(false);

        assertThrows(
            ResourceNotFoundException.class,
            () -> permissionService.assignPermissionToRole(roleId, permissionId)
        );

        verifyNoInteractions(rolePermissionRepository);
    }

    @Test
    void removePermissionFromRoleDelegatesToRepository() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();

        permissionService.removePermissionFromRole(roleId, permissionId);

        verify(rolePermissionRepository).deleteByRoleIdAndPermissionId(roleId, permissionId);
    }

    @Test
    void assignRoleToUserSavesNewAssignment() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        AuthUserRoleId assignmentId = new AuthUserRoleId(userId, roleId);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(roleRepository.existsById(roleId)).thenReturn(true);
        when(userRoleRepository.existsById(assignmentId)).thenReturn(false);

        permissionService.assignRoleToUser(userId, roleId);

        verify(userRoleRepository).save(any(AuthUserRole.class));
    }

    @Test
    void assignRoleToUserSkipsExistingAssignment() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        AuthUserRoleId assignmentId = new AuthUserRoleId(userId, roleId);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(roleRepository.existsById(roleId)).thenReturn(true);
        when(userRoleRepository.existsById(assignmentId)).thenReturn(true);

        permissionService.assignRoleToUser(userId, roleId);

        verify(userRoleRepository, never()).save(any(AuthUserRole.class));
    }

    @Test
    void assignRoleToUserRejectsMissingUser() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> permissionService.assignRoleToUser(userId, roleId));

        verifyNoInteractions(roleRepository, userRoleRepository);
    }

    @Test
    void assignRoleToUserRejectsMissingRole() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);
        when(roleRepository.existsById(roleId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> permissionService.assignRoleToUser(userId, roleId));

        verifyNoInteractions(userRoleRepository);
    }

    @Test
    void removeRoleFromUserDelegatesToRepository() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        permissionService.removeRoleFromUser(userId, roleId);

        verify(userRoleRepository).deleteByUserIdAndRoleId(userId, roleId);
    }

    @Test
    void assignPermissionToUserSavesNewAssignment() {
        UUID userId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        AuthUserPermissionId assignmentId = new AuthUserPermissionId(userId, permissionId);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(permissionRepository.existsById(permissionId)).thenReturn(true);
        when(userPermissionRepository.existsById(assignmentId)).thenReturn(false);

        permissionService.assignPermissionToUser(userId, permissionId);

        verify(userPermissionRepository).save(any(AuthUserPermission.class));
    }

    @Test
    void assignPermissionToUserSkipsExistingAssignment() {
        UUID userId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        AuthUserPermissionId assignmentId = new AuthUserPermissionId(userId, permissionId);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(permissionRepository.existsById(permissionId)).thenReturn(true);
        when(userPermissionRepository.existsById(assignmentId)).thenReturn(true);

        permissionService.assignPermissionToUser(userId, permissionId);

        verify(userPermissionRepository, never()).save(any(AuthUserPermission.class));
    }

    @Test
    void assignPermissionToUserRejectsMissingUser() {
        UUID userId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(
            ResourceNotFoundException.class,
            () -> permissionService.assignPermissionToUser(userId, permissionId)
        );

        verifyNoInteractions(permissionRepository, userPermissionRepository);
    }

    @Test
    void assignPermissionToUserRejectsMissingPermission() {
        UUID userId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);
        when(permissionRepository.existsById(permissionId)).thenReturn(false);

        assertThrows(
            ResourceNotFoundException.class,
            () -> permissionService.assignPermissionToUser(userId, permissionId)
        );

        verifyNoInteractions(userPermissionRepository);
    }

    @Test
    void removePermissionFromUserDelegatesToRepository() {
        UUID userId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();

        permissionService.removePermissionFromUser(userId, permissionId);

        verify(userPermissionRepository).deleteByUserIdAndPermissionId(userId, permissionId);
    }
}
