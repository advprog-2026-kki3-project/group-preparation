package id.ac.ui.cs.advprog.bidmart.auth.repository;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUserPermission;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUserPermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuthUserPermissionRepository extends JpaRepository<AuthUserPermission, AuthUserPermissionId> {
    List<AuthUserPermission> findByUserId(UUID userId);

    void deleteByUserIdAndPermissionId(UUID userId, UUID permissionId);
}
