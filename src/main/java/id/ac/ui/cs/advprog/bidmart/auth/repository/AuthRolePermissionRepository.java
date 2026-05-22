package id.ac.ui.cs.advprog.bidmart.auth.repository;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRolePermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthRolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuthRolePermissionRepository extends JpaRepository<AuthRolePermission, AuthRolePermissionId> {
    List<AuthRolePermission> findByRoleId(UUID roleId);

    void deleteByRoleIdAndPermissionId(UUID roleId, UUID permissionId);
}
