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
import id.ac.ui.cs.advprog.bidmart.order.dto.CreateOrderRequest;
import id.ac.ui.cs.advprog.bidmart.order.repository.OrderRepository;
import id.ac.ui.cs.advprog.bidmart.order.service.OrderService;
import id.ac.ui.cs.advprog.bidmart.wallet.service.WalletService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class AuctionServiceImpl implements AuctionService {

    private static final ZoneId AUCTION_ZONE = ZoneId.of("Asia/Jakarta");
    private static final NumberFormat IDR_FORMAT = NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"));

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final WalletService walletService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final Counter auctionsCreated;
    private final Counter bidSuccesses;
    private final Counter bidFailures;
    private final Counter bidsRejectedClosed;
    private final Counter bidsRejectedTooLow;
    private final Counter historyRequests;
    private final Counter bidEventsPublished;

    public AuctionServiceImpl(BidRepository bidRepository,
                              AuctionRepository auctionRepository,
                              ApplicationEventPublisher eventPublisher,
                              WalletService walletService,
                              OrderService orderService,
                              OrderRepository orderRepository,
                              MeterRegistry meterRegistry) {
        this.bidRepository = bidRepository;
        this.auctionRepository = auctionRepository;
        this.eventPublisher = eventPublisher;
        this.walletService = walletService;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.auctionsCreated = Counter.builder("bidmart.auction.created")
                .description("Total auctions created")
                .register(meterRegistry);
        this.bidSuccesses = Counter.builder("bidmart.auction.bid.success")
                .description("Total successful bids")
                .register(meterRegistry);
        this.bidFailures = Counter.builder("bidmart.auction.bid.failure")
                .description("Total failed bid attempts")
                .register(meterRegistry);
        this.bidsRejectedClosed = Counter.builder("bidmart.auction.bid.rejected_closed")
                .description("Total bids rejected because the auction is not accepting bids")
                .register(meterRegistry);
        this.bidsRejectedTooLow = Counter.builder("bidmart.auction.bid.rejected_too_low")
                .description("Total bids rejected because the amount is below the required minimum")
                .register(meterRegistry);
        this.historyRequests = Counter.builder("bidmart.auction.history.requests")
                .description("Total auction bidding history requests")
                .register(meterRegistry);
        this.bidEventsPublished = Counter.builder("bidmart.auction.events.bid_published")
                .description("Total bid placed events published")
                .register(meterRegistry);
    }

    @Override
    public List<BidResponseDTO> getBiddingHistory(String auctionId) {
        historyRequests.increment();
        List<Bid> bids = bidRepository.findByAuctionIdOrderByAmountDesc(auctionId);

        return bids.stream()
                .map(bid -> new BidResponseDTO(bid.getBidderId(), bid.getAmount(), bid.getTimestamp()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AuctionResponseDTO getAuctionByListingId(String listingId) {
        Auction auction = auctionRepository.findByCatalogueListingId(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        settleAuctionIfEnded(auction);

        return toResponse(auction);
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
        auctionsCreated.increment();

        return toResponse(savedAuction);
    }

    @Override
    @Transactional
    public BidResponseDTO placeBid(String auctionId, PlaceBidRequestDTO request) {
        try {
            return placeBidInternal(auctionId, request);
        } catch (RuntimeException exception) {
            bidFailures.increment();
            throw exception;
        }
    }

    @Override
    @Scheduled(fixedDelayString = "${app.auction.settlement-interval-ms:30000}")
    @Transactional
    public int settleEndedAuctions() {
        List<Auction> endedAuctions = auctionRepository.findByStageInAndEndTimeLessThanEqual(
                List.of(AuctionStage.ACTIVE, AuctionStage.EXTENDED, AuctionStage.WON),
                nowInAuctionZone()
        );

        endedAuctions.forEach(this::settleAuctionIfEnded);
        return endedAuctions.size();
    }

    private BidResponseDTO placeBidInternal(String auctionId, PlaceBidRequestDTO request) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        settleAuctionIfEnded(auction);

        if (auction.getSellerId() != null && auction.getSellerId().equals(request.getBidderId())) {
            throw new IllegalStateException("Sellers cannot bid on their own listing.");
        }

        if (!auction.isAcceptingBids()) {
            bidsRejectedClosed.increment();
            throw new IllegalStateException("Bids are not allowed. Current stage: " + auction.getStage());
        }

        Bid previousHighestBid = bidRepository.findByAuctionIdOrderByAmountDesc(auctionId)
                .stream()
                .findFirst()
                .orElse(null);

        double minimumIncrement = 5.0;
        double requiredMinimumBid = auction.getCurrentHighestBid() + minimumIncrement;

        if (auction.getCurrentHighestBid() == 0.0) {
            requiredMinimumBid = auction.getInitialPrice();
        }

        if (request.getAmount() < requiredMinimumBid) {
            bidsRejectedTooLow.increment();
            throw new IllegalArgumentException("Bid amount must be at least Rp " + IDR_FORMAT.format(requiredMinimumBid));
        }

        long amountToHold = request.getAmount().longValue();
        if (previousHighestBid != null && previousHighestBid.getBidderId().equals(request.getBidderId())) {
            amountToHold = request.getAmount().longValue() - previousHighestBid.getAmount().longValue();
        }
        if (amountToHold > 0) {
            walletService.holdFunds(request.getBidderId(), amountToHold);
        }

        Bid newBid = new Bid();
        newBid.setAuctionId(auction.getId());
        newBid.setBidderId(request.getBidderId());
        newBid.setAmount(request.getAmount());
        newBid.setTimestamp(nowInAuctionZone());

        bidRepository.save(newBid);

        auction.setCurrentHighestBid(request.getAmount());
        auctionRepository.save(auction);

        if (previousHighestBid != null && !previousHighestBid.getBidderId().equals(request.getBidderId())) {
            walletService.releaseFunds(previousHighestBid.getBidderId(), previousHighestBid.getAmount().longValue());
        }

        BidPlacedEvent event = new BidPlacedEvent(
                auction.getId(),
                newBid.getBidderId(),
                newBid.getAmount(),
                newBid.getTimestamp()
        );
        eventPublisher.publishEvent(event);
        bidEventsPublished.increment();
        bidSuccesses.increment();

        return new BidResponseDTO(newBid.getBidderId(), newBid.getAmount(), newBid.getTimestamp());
    }

    private void settleAuctionIfEnded(Auction auction) {
        if (auction.getEndTime() == null || auction.getEndTime().isAfter(nowInAuctionZone())) {
            return;
        }
        if (auction.getStage() == AuctionStage.WON) {
            ensureOrderExistsForWonAuction(auction);
            return;
        }
        if (auction.getStage() == AuctionStage.UNSOLD) {
            return;
        }

        Bid winningBid = bidRepository.findByAuctionIdOrderByAmountDesc(auction.getId())
                .stream()
                .findFirst()
                .orElse(null);

        if (winningBid == null || winningBid.getAmount() < auction.getReservePrice()) {
            auction.setStage(AuctionStage.UNSOLD);
            if (winningBid != null) {
                walletService.releaseFunds(winningBid.getBidderId(), winningBid.getAmount().longValue());
            }
        } else {
            auction.setStage(AuctionStage.WON);
            auction.setWinnerId(winningBid.getBidderId());
            createPaidOrderForWinner(auction, winningBid);
        }

        auctionRepository.save(auction);
    }

    private void createPaidOrderForWinner(Auction auction, Bid winningBid) {
        if (orderRepository.existsByAuctionId(auction.getId())) {
            return;
        }

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAuctionId(auction.getId());
        request.setWinnerUsername(winningBid.getBidderId());
        request.setSellerUsername(auction.getSellerId());
        request.setShippingAddress("Address pending");
        request.setAmount(winningBid.getAmount().longValue());
        orderService.createPaidOrder(request);
    }

    private void ensureOrderExistsForWonAuction(Auction auction) {
        if (orderRepository.existsByAuctionId(auction.getId())) {
            return;
        }

        Bid winningBid = bidRepository.findByAuctionIdOrderByAmountDesc(auction.getId())
                .stream()
                .findFirst()
                .orElse(null);

        if (winningBid != null && winningBid.getBidderId().equals(auction.getWinnerId())) {
            createPaidOrderForWinner(auction, winningBid);
        }
    }

    private AuctionResponseDTO toResponse(Auction auction) {
        return new AuctionResponseDTO(
                auction.getId(),
                auction.getSellerId(),
                auction.getCatalogueListingId(),
                auction.getInitialPrice(),
                auction.getReservePrice(),
                auction.getCurrentHighestBid(),
                auction.getWinnerId(),
                auction.getStartTime(),
                auction.getEndTime(),
                auction.getStage()
        );
    }

    private LocalDateTime nowInAuctionZone() {
        return LocalDateTime.now(AUCTION_ZONE);
    }
}
