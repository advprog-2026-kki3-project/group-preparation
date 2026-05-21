package id.ac.ui.cs.advprog.bidmart.auth.config;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRolePermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRolePermissionId;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserRole;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthPermissionRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthRolePermissionRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthRoleRepository;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthUserRepository;

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
            AuthUserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            AuthRole adminRole = ensureRole(roleRepository, "ADMINISTRATOR", "Built-in administrator role", true);
            AuthRole sellerRole = ensureRole(roleRepository, "SELLER", "Built-in seller role", true);
            AuthRole buyerRole = ensureRole(roleRepository, "BUYER", "Built-in buyer role", true);

            AuthPermission adminPermission = ensurePermission(permissionRepository, "auth:admin", "Manage auth");

            AuthPermission walletView = ensurePermission(permissionRepository, "wallet:view", "View wallet balance");
            AuthPermission walletHistory = ensurePermission(permissionRepository, "wallet:history", "View wallet history");
            AuthPermission walletTopup = ensurePermission(permissionRepository, "wallet:topup", "Top up wallet");
            AuthPermission auctionCreate = ensurePermission(permissionRepository, "auction:create", "Create auctions");

            assignPermission(rolePermissionRepository, adminRole, adminPermission);

            assignPermission(rolePermissionRepository, buyerRole, walletView);
            assignPermission(rolePermissionRepository, buyerRole, walletHistory);
            assignPermission(rolePermissionRepository, buyerRole, walletTopup);

            assignPermission(rolePermissionRepository, sellerRole, walletView);
            assignPermission(rolePermissionRepository, sellerRole, walletHistory);
            assignPermission(rolePermissionRepository, sellerRole, walletTopup);
            assignPermission(rolePermissionRepository, sellerRole, auctionCreate);

            if (userRepository.findByEmailIgnoreCase("admin@bidmart.com").isEmpty()) {
                AuthUser admin = new AuthUser();
                admin.setEmail("admin@bidmart.com");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setPrimaryRole(UserRole.ADMINISTRATOR);
                userRepository.save(admin);
            }

            if (userRepository.findByEmailIgnoreCase("bidmart.project.int@gmail.com").isEmpty()) {
                AuthUser testUser = new AuthUser();
                testUser.setEmail("bidmart.project.int@gmail.com");
                testUser.setPasswordHash(passwordEncoder.encode("bidmart123"));
                testUser.setPrimaryRole(UserRole.SELLER);
                userRepository.save(testUser);
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

    private void assignPermission(AuthRolePermissionRepository repo, AuthRole role, AuthPermission perm) {
        AuthRolePermissionId assignmentId = new AuthRolePermissionId(role.getId(), perm.getId());
        if (!repo.existsById(assignmentId)) {
            repo.save(new AuthRolePermission(role.getId(), perm.getId()));
        }
    }
}