package id.ac.ui.cs.advprog.bidmart.auth.config;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRolePermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRolePermissionId;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthPermissionRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthRolePermissionRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthRoleRepository;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthDataInitializer {

    @Bean
    ApplicationRunner initializeAuthData(
            AuthRoleRepository roleRepository,
            AuthPermissionRepository permissionRepository,
            AuthRolePermissionRepository rolePermissionRepository
    ) {
        return args -> {
            AuthRole adminRole = ensureRole(roleRepository, "ADMINISTRATOR", "Built-in administrator role", true);
            AuthRole sellerRole = ensureRole(roleRepository, "SELLER", "Built-in seller role", true);
            AuthRole buyerRole = ensureRole(roleRepository, "BUYER", "Built-in buyer role", true);

            AuthPermission adminPermission = ensurePermission(permissionRepository, "auth:admin", "Manage auth");

            AuthPermission walletView = ensurePermission(permissionRepository, "wallet:view", "View wallet balance");
            AuthPermission walletCreate = ensurePermission(permissionRepository, "wallet:create", "Create wallet transactions");

            AuthPermission catalogueCreate = ensurePermission(permissionRepository, "catalogue:create", "Create catalogue listings");
            AuthPermission catalogueUpdate = ensurePermission(permissionRepository, "catalogue:update", "Update catalogue listings");
            AuthPermission catalogueDelete = ensurePermission(permissionRepository, "catalogue:delete", "Delete catalogue listings");

            assignPermission(rolePermissionRepository, adminRole, adminPermission);
            assignPermission(rolePermissionRepository, adminRole, walletView);
            assignPermission(rolePermissionRepository, adminRole, walletCreate);

            assignPermission(rolePermissionRepository, buyerRole, walletView);
            assignPermission(rolePermissionRepository, buyerRole, walletCreate);

            assignPermission(rolePermissionRepository, sellerRole, walletView);
            assignPermission(rolePermissionRepository, sellerRole, walletCreate);

            assignPermission(rolePermissionRepository, sellerRole, catalogueCreate);
            assignPermission(rolePermissionRepository, sellerRole, catalogueUpdate);
            assignPermission(rolePermissionRepository, sellerRole, catalogueDelete);
        };
    }

    private AuthRole ensureRole(AuthRoleRepository roleRepository, String name, String description, boolean systemRole) {
        return roleRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            AuthRole role = new AuthRole();
            role.setName(name);
            role.setDescription(description);
            role.setSystemRole(systemRole);
            return roleRepository.save(role);
        });
    }

    private AuthPermission ensurePermission(AuthPermissionRepository permissionRepository, String name, String description) {
        return permissionRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            AuthPermission permission = new AuthPermission();
            permission.setName(name);
            permission.setDescription(description);
            return permissionRepository.save(permission);
        });
    }

    private void assignPermission(AuthRolePermissionRepository repo, AuthRole role, AuthPermission perm) {
        AuthRolePermissionId assignmentId = new AuthRolePermissionId(role.getId(), perm.getId());
        if (!repo.existsById(assignmentId)) {
            repo.save(new AuthRolePermission(role.getId(), perm.getId()));
        }
    }
}
