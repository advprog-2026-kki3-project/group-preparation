package id.ac.ui.cs.advprog.bidmart.auth.repository;

import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallenge;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallengePurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TwoFactorChallengeRepository extends JpaRepository<TwoFactorChallenge, UUID> {
    Optional<TwoFactorChallenge> findByIdAndConsumedAtIsNull(UUID id);

    @Modifying
    @Query("update TwoFactorChallenge c set c.attempts = c.attempts + 1 where c.id = :id and c.consumedAt is null")
    int incrementAttempts(@Param("id") UUID id);

    @Query("""
        select coalesce(sum(c.attempts), 0)
        from TwoFactorChallenge c
        where c.user.id = :userId
          and c.purpose = :purpose
          and c.createdAt > :createdAfter
        """)
    long sumAttemptsByUserAndPurposeSince(
        @Param("userId") UUID userId,
        @Param("purpose") TwoFactorChallengePurpose purpose,
        @Param("createdAfter") Instant createdAfter
    );

    Optional<TwoFactorChallenge> findFirstByUser_IdAndPurposeAndAttemptsGreaterThanAndCreatedAtAfterOrderByCreatedAtAsc(
        UUID userId,
        TwoFactorChallengePurpose purpose,
        int attempts,
        Instant createdAfter
    );
}
