package id.ac.ui.cs.advprog.bidmart.auth.repository;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUserRole;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuthUserRoleRepository extends JpaRepository<AuthUserRole, AuthUserRoleId> {
    List<AuthUserRole> findByUserId(UUID userId);

    void deleteByUserIdAndRoleId(UUID userId, UUID roleId);
}
