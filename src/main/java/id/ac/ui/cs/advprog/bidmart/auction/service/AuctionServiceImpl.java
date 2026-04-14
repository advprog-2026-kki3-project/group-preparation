package id.ac.ui.cs.advprog.bidmart.auction.service;

import id.ac.ui.cs.advprog.bidmart.auction.dto.AuctionResponseDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.BidResponseDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.CreateAuctionRequestDTO;
import id.ac.ui.cs.advprog.bidmart.auction.model.Auction;
import id.ac.ui.cs.advprog.bidmart.auction.model.AuctionStage;
import id.ac.ui.cs.advprog.bidmart.auction.model.Bid;
import id.ac.ui.cs.advprog.bidmart.auction.repository.AuctionRepository;
import id.ac.ui.cs.advprog.bidmart.auction.repository.BidRepository;
import id.ac.ui.cs.advprog.bidmart.auction.dto.PlaceBidRequestDTO;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuctionServiceImpl implements AuctionService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository; // NEW

    public AuctionServiceImpl(BidRepository bidRepository, AuctionRepository auctionRepository) {
        this.bidRepository = bidRepository;
        this.auctionRepository = auctionRepository;
    }

    @Override
    public List<BidResponseDTO> getBiddingHistory(String auctionId) {
        List<Bid> bids = bidRepository.findByAuctionIdOrderByAmountDesc(auctionId);
        return bids.stream()
                .map(bid -> new BidResponseDTO(bid.getBidderId(), bid.getAmount(), bid.getTimestamp()))
                .collect(Collectors.toList());
    }

    @Override
    public AuctionResponseDTO createAuction(CreateAuctionRequestDTO request) {
        // 1. This is to map the incoming Request DTO to our Database Entity
        Auction auction = new Auction();
        auction.setSellerId(request.getSellerId());
        auction.setCatalogueListingId(request.getCatalogueListingId());
        auction.setInitialPrice(request.getInitialPrice());
        auction.setReservePrice(request.getReservePrice());
        auction.setStartTime(request.getStartTime());
        auction.setEndTime(request.getEndTime());

        // 2. Setting the default system values for a brand new auction
        auction.setStage(AuctionStage.DRAFT);
        auction.setCurrentHighestBid(0.0);

        // 3. Then saving it to the database.
        Auction savedAuction = auctionRepository.save(auction);

        // 4. This should map the saved Entity back to a Response DTO
        return new AuctionResponseDTO(
                savedAuction.getId(),
                savedAuction.getSellerId(),
                savedAuction.getCatalogueListingId(),
                savedAuction.getInitialPrice(),
                savedAuction.getReservePrice(),
                savedAuction.getCurrentHighestBid(),
                savedAuction.getStartTime(),
                savedAuction.getEndTime(),
                savedAuction.getStage()
        );
    }

    @Override
    @Transactional
    public BidResponseDTO placeBid(String auctionId, PlaceBidRequestDTO request) {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        if (!auction.isAcceptingBids()) {
            throw new IllegalStateException("Bids are not allowed. Current stage: " + auction.getStage());
        }

        double minimumIncrement = 5.0;
        double requiredMinimumBid = auction.getCurrentHighestBid() + minimumIncrement;

        if (request.getAmount() < requiredMinimumBid) {
            throw new IllegalArgumentException("Bid amount must be at least " + requiredMinimumBid);
        }

        // TODO: Wallet Integration (Milestone 2 requires mocking this)
        // For now, we assume the user has enough balance and funds are "held".
        System.out.println("Mocking Wallet: Held " + request.getAmount() + " for user " + request.getBidderId());

        Bid newBid = new Bid();
        newBid.setAuctionId(auction.getId());
        newBid.setBidderId(request.getBidderId());
        newBid.setAmount(request.getAmount());
        newBid.setTimestamp(LocalDateTime.now());

        bidRepository.save(newBid);

        auction.setCurrentHighestBid(request.getAmount());
        auctionRepository.save(auction);

        return new BidResponseDTO(newBid.getBidderId(), newBid.getAmount(), newBid.getTimestamp());
    }
}