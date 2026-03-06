package id.ac.ui.cs.advprog.bidmart.auth.repository;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {
    List<AuthSession> findByUser_IdAndRevokedAtIsNullOrderByCreatedAtAsc(UUID userId);

    long countByUser_IdAndRevokedAtIsNull(UUID userId);

    Optional<AuthSession> findFirstByUser_IdAndRevokedAtIsNullOrderByCreatedAtAsc(UUID userId);

    @Query("select s from AuthSession s join fetch s.user where s.id = :id and s.revokedAt is null")
    Optional<AuthSession> findActiveByIdWithUser(@Param("id") UUID id);
}
