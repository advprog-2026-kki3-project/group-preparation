package id.ac.ui.cs.advprog.bidmart.auction.repository;

import id.ac.ui.cs.advprog.bidmart.auction.model.Auction;
import id.ac.ui.cs.advprog.bidmart.auction.model.AuctionStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, String> {
    Optional<Auction> findByCatalogueListingId(String catalogueListingId);

    List<Auction> findByStageInAndEndTimeLessThanEqual(Collection<AuctionStage> stages, LocalDateTime endTime);
}
