package id.ac.ui.cs.advprog.bidmart.auth.repository;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthPermissionRepository extends JpaRepository<AuthPermission, UUID> {
    Optional<AuthPermission> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<AuthPermission> findAllByOrderByNameAsc();

    @Query("""
        select distinct p.name
        from AuthPermission p
        join AuthUserPermission up on up.permissionId = p.id
        where up.userId = :userId
        """)
    List<String> findDirectPermissionNamesByUserId(UUID userId);

    @Query("""
        select distinct p.name
        from AuthPermission p
        join AuthRolePermission rp on rp.permissionId = p.id
        join AuthUserRole ur on ur.roleId = rp.roleId
        where ur.userId = :userId
        """)
    List<String> findRolePermissionNamesByUserId(UUID userId);
}
