package id.ac.ui.cs.advprog.bidmart.auction.repository;

import id.ac.ui.cs.advprog.bidmart.auction.model.Auction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, String> {
    Optional<Auction> findByCatalogueListingId(String catalogueListingId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Auction a WHERE a.id = :id")
    Optional<Auction> findByIdWithLock(@Param("id") String id);
}