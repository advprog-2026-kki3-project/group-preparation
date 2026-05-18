package id.ac.ui.cs.advprog.bidmart.auth.repository;

import id.ac.ui.cs.advprog.bidmart.auth.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {
    long countByEmailAndSuccessfulFalseAndAttemptedAtAfter(String email, Instant attemptedAfter);

    Optional<LoginAttempt> findFirstByEmailAndSuccessfulFalseAndAttemptedAtAfterOrderByAttemptedAtAsc(
        String email,
        Instant attemptedAfter
    );
}
