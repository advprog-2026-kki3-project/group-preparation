package id.ac.ui.cs.advprog.bidmart.auth.config;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRolePermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRolePermissionId;

// --- THE NEW IMPORTS ---
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUserRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUserRoleId;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserRole;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthPermissionRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthRolePermissionRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthRoleRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthUserRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthUserRoleRepository;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthDataInitializer {

    @Bean
    ApplicationRunner initializeAuthData(
            AuthRoleRepository roleRepository,
            AuthPermissionRepository permissionRepository,
            AuthRolePermissionRepository rolePermissionRepository,
            AuthUserRoleRepository userRoleRepository,
            AuthUserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            AuthRole adminRole = ensureRole(roleRepository, "ADMINISTRATOR", "Built-in administrator role", true);
            AuthRole sellerRole = ensureRole(roleRepository, "SELLER", "Built-in seller role", true);
            AuthRole buyerRole = ensureRole(roleRepository, "BUYER", "Built-in buyer role", true);

            AuthPermission adminPermission = ensurePermission(
                    permissionRepository,
                    "auth:admin",
                    "Manage authentication users, roles, and permissions"
            );
            AuthPermission walletViewPermission = ensurePermission(
                    permissionRepository,
                    "wallet:view",
                    "View wallet balance and transaction history"
            );
            AuthPermission walletCreatePermission = ensurePermission(
                    permissionRepository,
                    "wallet:create",
                    "Create wallet transactions"
            );

            ensureRolePermission(rolePermissionRepository, adminRole, adminPermission);
            ensureRolePermission(rolePermissionRepository, adminRole, walletViewPermission);
            ensureRolePermission(rolePermissionRepository, adminRole, walletCreatePermission);
            ensureRolePermission(rolePermissionRepository, sellerRole, walletViewPermission);
            ensureRolePermission(rolePermissionRepository, sellerRole, walletCreatePermission);
            ensureRolePermission(rolePermissionRepository, buyerRole, walletViewPermission);
            ensureRolePermission(rolePermissionRepository, buyerRole, walletCreatePermission);

            if (userRepository.findByEmailIgnoreCase("admin@bidmart.com").isEmpty()) {
                AuthUser admin = new AuthUser();

                admin.setEmail("admin@bidmart.com");

                admin.setPasswordHash(passwordEncoder.encode("admin123"));

                admin.setPrimaryRole(UserRole.ADMINISTRATOR);

                AuthUser savedAdmin = userRepository.save(admin);
                ensureUserRole(userRoleRepository, savedAdmin, adminRole);
            } else {
                userRepository.findByEmailIgnoreCase("admin@bidmart.com")
                        .ifPresent(admin -> ensureUserRole(userRoleRepository, admin, adminRole));
            }
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

    private void ensureRolePermission(
            AuthRolePermissionRepository rolePermissionRepository,
            AuthRole role,
            AuthPermission permission
    ) {
        AuthRolePermissionId assignmentId = new AuthRolePermissionId(role.getId(), permission.getId());
        if (!rolePermissionRepository.existsById(assignmentId)) {
            rolePermissionRepository.save(new AuthRolePermission(role.getId(), permission.getId()));
        }
    }

    private void ensureUserRole(AuthUserRoleRepository userRoleRepository, AuthUser user, AuthRole role) {
        AuthUserRoleId assignmentId = new AuthUserRoleId(user.getId(), role.getId());
        if (!userRoleRepository.existsById(assignmentId)) {
            userRoleRepository.save(new AuthUserRole(user.getId(), role.getId()));
        }
    }
}
