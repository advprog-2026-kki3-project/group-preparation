package id.ac.ui.cs.advprog.bidmart.auction.service;

import id.ac.ui.cs.advprog.bidmart.auction.dto.BidResponseDTO;
import java.util.List;

public interface AuctionService {
    List<BidResponseDTO> getBiddingHistory(String auctionId);
}