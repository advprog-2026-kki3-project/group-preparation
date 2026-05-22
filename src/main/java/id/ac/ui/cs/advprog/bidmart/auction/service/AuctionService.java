package id.ac.ui.cs.advprog.bidmart.auction.service;
import id.ac.ui.cs.advprog.bidmart.auction.dto.AuctionResponseDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.BidResponseDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.CreateAuctionRequestDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.PlaceBidRequestDTO;
import java.util.List;

public interface AuctionService {
    List<BidResponseDTO> getBiddingHistory(String auctionId);
    AuctionResponseDTO createAuction(CreateAuctionRequestDTO request);
    BidResponseDTO placeBid(String auctionId, PlaceBidRequestDTO request);
}