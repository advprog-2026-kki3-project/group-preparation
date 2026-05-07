package id.ac.ui.cs.advprog.bidmart.auth.config;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRole;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthPermissionRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthRolePermissionRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthRoleRepository;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRolePermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRolePermissionId;
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
            ensureRole(roleRepository, "SELLER", "Built-in seller role", true);
            ensureRole(roleRepository, "BUYER", "Built-in buyer role", true);
            AuthPermission adminPermission = ensurePermission(
                permissionRepository,
                "auth:admin",
                "Manage authentication users, roles, and permissions"
            );
            AuthRolePermissionId assignmentId = new AuthRolePermissionId(adminRole.getId(), adminPermission.getId());
            if (!rolePermissionRepository.existsById(assignmentId)) {
                rolePermissionRepository.save(new AuthRolePermission(adminRole.getId(), adminPermission.getId()));
            }
        };
    }

    private AuthRole ensureRole(
        AuthRoleRepository roleRepository,
        String name,
        String description,
        boolean systemRole
    ) {
        return roleRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            AuthRole role = new AuthRole();
            role.setName(name);
            role.setDescription(description);
            role.setSystemRole(systemRole);
            return roleRepository.save(role);
        });
    }

    private AuthPermission ensurePermission(
        AuthPermissionRepository permissionRepository,
        String name,
        String description
    ) {
        return permissionRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            AuthPermission permission = new AuthPermission();
            permission.setName(name);
            permission.setDescription(description);
            return permissionRepository.save(permission);
        });
    }
}
