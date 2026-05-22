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

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        if (!auction.isAcceptingBids()) {
            throw new IllegalStateException("Bids are not allowed. Current stage: " + auction.getStage());
        }

        double minimumIncrement = 5.0;
        double requiredMinimumBid = auction.getCurrentHighestBid() + minimumIncrement;

        if (auction.getCurrentHighestBid() == 0.0) {
            requiredMinimumBid = auction.getInitialPrice();
        }

        if (request.getAmount() < requiredMinimumBid) {
            throw new IllegalArgumentException("Bid amount must be at least $" + requiredMinimumBid);
        }

        // Real Wallet Integration: Hold funds before finalizing the bid
        walletService.holdFunds(request.getBidderId(), request.getAmount().longValue());

        Bid newBid = new Bid();
        newBid.setAuctionId(auction.getId());
        newBid.setBidderId(request.getBidderId());
        newBid.setAmount(request.getAmount());
        newBid.setTimestamp(LocalDateTime.now());

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
}