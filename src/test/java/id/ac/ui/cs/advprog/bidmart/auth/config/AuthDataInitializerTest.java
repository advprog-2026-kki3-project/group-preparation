package id.ac.ui.cs.advprog.bidmart.auth.config;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRolePermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRolePermissionId;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthPermissionRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthRolePermissionRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthRoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthDataInitializerTest {

    @Test
    void initializeAuthDataSkipsCreationWhenDataAlreadyExists() throws Exception {
        AuthRoleRepository roleRepository = mock(AuthRoleRepository.class);
        AuthPermissionRepository permissionRepository = mock(AuthPermissionRepository.class);
        AuthRolePermissionRepository rolePermissionRepository = mock(AuthRolePermissionRepository.class);

        AuthRole adminRole = role("ADMINISTRATOR");
        AuthRole sellerRole = role("SELLER");
        AuthRole buyerRole = role("BUYER");
        AuthPermission adminPermission = permission("auth:admin");
        AuthPermission walletViewPermission = permission("wallet:view");
        AuthPermission walletCreatePermission = permission("wallet:create");
        AuthPermission catalogueCreatePermission = permission("catalogue:create");
        AuthPermission catalogueUpdatePermission = permission("catalogue:update");
        AuthPermission catalogueDeletePermission = permission("catalogue:delete");

        when(roleRepository.findByNameIgnoreCase("ADMINISTRATOR")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByNameIgnoreCase("SELLER")).thenReturn(Optional.of(sellerRole));
        when(roleRepository.findByNameIgnoreCase("BUYER")).thenReturn(Optional.of(buyerRole));
        when(permissionRepository.findByNameIgnoreCase("auth:admin")).thenReturn(Optional.of(adminPermission));
        when(permissionRepository.findByNameIgnoreCase("wallet:view")).thenReturn(Optional.of(walletViewPermission));
        when(permissionRepository.findByNameIgnoreCase("wallet:create")).thenReturn(Optional.of(walletCreatePermission));
        when(permissionRepository.findByNameIgnoreCase("catalogue:create")).thenReturn(Optional.of(catalogueCreatePermission));
        when(permissionRepository.findByNameIgnoreCase("catalogue:update")).thenReturn(Optional.of(catalogueUpdatePermission));
        when(permissionRepository.findByNameIgnoreCase("catalogue:delete")).thenReturn(Optional.of(catalogueDeletePermission));
        when(rolePermissionRepository.existsById(any(AuthRolePermissionId.class))).thenReturn(true);

        ApplicationRunner runner = new AuthDataInitializer().initializeAuthData(
            roleRepository,
            permissionRepository,
            rolePermissionRepository
        );

        runner.run(null);

        verify(roleRepository, never()).save(any(AuthRole.class));
        verify(permissionRepository, never()).save(any(AuthPermission.class));
        verify(rolePermissionRepository, never()).save(any(AuthRolePermission.class));
    }

    private static AuthRole role(String name) {
        AuthRole role = new AuthRole();
        ReflectionTestUtils.setField(role, "id", UUID.randomUUID());
        role.setName(name);
        role.setDescription(name + " role");
        role.setSystemRole(true);
        return role;
    }

    private static AuthPermission permission(String name) {
        AuthPermission permission = new AuthPermission();
        ReflectionTestUtils.setField(permission, "id", UUID.randomUUID());
        permission.setName(name);
        permission.setDescription(name + " permission");
        return permission;
    }

}
