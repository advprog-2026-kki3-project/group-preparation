package id.ac.ui.cs.advprog.bidmart.auth.repository;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {
    Optional<AuthUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
