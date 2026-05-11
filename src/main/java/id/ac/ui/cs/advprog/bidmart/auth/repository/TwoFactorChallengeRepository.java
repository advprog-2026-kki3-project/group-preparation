package id.ac.ui.cs.advprog.bidmart.auth.repository;

import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TwoFactorChallengeRepository extends JpaRepository<TwoFactorChallenge, UUID> {
    Optional<TwoFactorChallenge> findByIdAndConsumedAtIsNull(UUID id);
}
