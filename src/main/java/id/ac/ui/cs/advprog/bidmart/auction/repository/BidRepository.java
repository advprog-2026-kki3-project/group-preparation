package id.ac.ui.cs.advprog.bidmart.auction.repository;

import id.ac.ui.cs.advprog.bidmart.auction.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, String> {
    List<Bid> findByAuctionIdOrderByAmountDesc(String auctionId);
}