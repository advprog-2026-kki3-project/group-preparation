package id.ac.ui.cs.advprog.bidmart.auction.service;

import id.ac.ui.cs.advprog.bidmart.auction.dto.AuctionResponseDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.BidResponseDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.CreateAuctionRequestDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.PlaceBidRequestDTO;
import id.ac.ui.cs.advprog.bidmart.auction.event.BidPlacedEvent;
import id.ac.ui.cs.advprog.bidmart.auction.model.Auction;
import id.ac.ui.cs.advprog.bidmart.auction.model.AuctionStage;
import id.ac.ui.cs.advprog.bidmart.auction.model.Bid;
import id.ac.ui.cs.advprog.bidmart.auction.repository.AuctionRepository;
import id.ac.ui.cs.advprog.bidmart.auction.repository.BidRepository;
import id.ac.ui.cs.advprog.bidmart.wallet.WalletService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuctionServiceImpl implements AuctionService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final WalletService walletService;

    public AuctionServiceImpl(BidRepository bidRepository,
                              AuctionRepository auctionRepository,
                              ApplicationEventPublisher eventPublisher,
                              WalletService walletService) {
        this.bidRepository = bidRepository;
        this.auctionRepository = auctionRepository;
        this.eventPublisher = eventPublisher;
        this.walletService = walletService;
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
        Auction auction = new Auction();
        auction.setSellerId(request.getSellerId());
        auction.setCatalogueListingId(request.getCatalogueListingId());
        auction.setInitialPrice(request.getInitialPrice());
        auction.setReservePrice(request.getReservePrice());
        auction.setStartTime(request.getStartTime());
        auction.setEndTime(request.getEndTime());

        auction.setCurrentHighestBid(0.0);

        auction.setStage(AuctionStage.ACTIVE);

        Auction savedAuction = auctionRepository.save(auction);

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

        Auction auction = auctionRepository.findByIdWithLock(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(auction.getEndTime())) {
            throw new IllegalStateException("Auction has ended.");
        }

        if (!auction.isAcceptingBids()) {
            throw new IllegalStateException("Bids are not allowed. Current stage: " + auction.getStage());
        }

        double minimumIncrement = 5.0;
        double requiredMinimumBid = auction.getCurrentHighestBid() + minimumIncrement;

        if (auction.getCurrentHighestBid() == 0.0) {
            requiredMinimumBid = auction.getInitialPrice();
        }

        if (request.getAmount() < requiredMinimumBid) {
            throw new IllegalArgumentException("Bid amount must be at least Rp " + requiredMinimumBid);
        }

        // Real Wallet Integration: Hold funds for the new bid
        walletService.holdFunds(request.getBidderId(), request.getAmount().longValue());

        // Find previous highest bidder to release their held funds
        Bid prevHighestBid = bidRepository.findFirstByAuctionIdOrderByAmountDesc(auction.getId()).orElse(null);

        if (prevHighestBid != null) {
            // Release funds for the outbid user
            walletService.releaseFunds(prevHighestBid.getBidderId(), prevHighestBid.getAmount().longValue());
        }

        // Extension logic: if bid is within 2 minutes of end time, extend by 2 minutes
        if (now.isAfter(auction.getEndTime().minusMinutes(2))) {
            auction.setEndTime(auction.getEndTime().plusMinutes(2));
            auction.setStage(AuctionStage.EXTENDED);
        }

        Bid newBid = new Bid();
        newBid.setAuctionId(auction.getId());
        newBid.setBidderId(request.getBidderId());
        newBid.setAmount(request.getAmount());
        newBid.setTimestamp(now);

        bidRepository.save(newBid);

        auction.setCurrentHighestBid(request.getAmount());
        auctionRepository.save(auction);

        BidPlacedEvent event = new BidPlacedEvent(
                auction.getId(),
                newBid.getBidderId(),
                newBid.getAmount(),
                newBid.getTimestamp()
        );
        eventPublisher.publishEvent(event);

        return new BidResponseDTO(newBid.getBidderId(), newBid.getAmount(), newBid.getTimestamp());
    }

    @Override
    @Transactional
    public AuctionResponseDTO finalizeAuction(String auctionId) {
        Auction auction = auctionRepository.findByIdWithLock(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        if (!LocalDateTime.now().isAfter(auction.getEndTime())) {
            throw new IllegalStateException("Auction has not ended yet.");
        }
        
        if (auction.getStage() == AuctionStage.WON || auction.getStage() == AuctionStage.UNSOLD) {
            throw new IllegalStateException("Auction is already finalized.");
        }

        if (auction.getCurrentHighestBid() >= auction.getReservePrice()) {
            auction.setStage(AuctionStage.WON);
            
            // Commit payment for the winner
            Bid highestBid = bidRepository.findFirstByAuctionIdOrderByAmountDesc(auction.getId()).orElse(null);
            if (highestBid != null) {
                walletService.commitPayment(highestBid.getBidderId(), highestBid.getAmount().longValue());
            }
        } else {
            auction.setStage(AuctionStage.UNSOLD);
            
            // Release funds since reserve price was not met
            Bid highestBid = bidRepository.findFirstByAuctionIdOrderByAmountDesc(auction.getId()).orElse(null);
            if (highestBid != null) {
                walletService.releaseFunds(highestBid.getBidderId(), highestBid.getAmount().longValue());
            }
        }

        auctionRepository.save(auction);

        return new AuctionResponseDTO(
                auction.getId(),
                auction.getSellerId(),
                auction.getCatalogueListingId(),
                auction.getInitialPrice(),
                auction.getReservePrice(),
                auction.getCurrentHighestBid(),
                auction.getStartTime(),
                auction.getEndTime(),
                auction.getStage()
        );
    }
}