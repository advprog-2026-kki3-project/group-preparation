package id.ac.ui.cs.advprog.bidmart.auction.service;

import id.ac.ui.cs.advprog.bidmart.auction.model.Auction;
import id.ac.ui.cs.advprog.bidmart.auction.model.AuctionStage;
import id.ac.ui.cs.advprog.bidmart.auction.model.Bid;

import id.ac.ui.cs.advprog.bidmart.auction.repository.BidRepository;
import id.ac.ui.cs.advprog.bidmart.auction.repository.AuctionRepository;

import id.ac.ui.cs.advprog.bidmart.auction.dto.CreateAuctionRequestDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.AuctionResponseDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.BidResponseDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.PlaceBidRequestDTO;

import id.ac.ui.cs.advprog.bidmart.auction.event.BidPlacedEvent;
import id.ac.ui.cs.advprog.bidmart.order.repository.OrderRepository;
import id.ac.ui.cs.advprog.bidmart.order.service.OrderService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.context.ApplicationEventPublisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuctionServiceImplTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private id.ac.ui.cs.advprog.bidmart.wallet.service.WalletService walletService;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    private AuctionServiceImpl auctionService;
    private SimpleMeterRegistry meterRegistry;

    private Bid mockBid1;
    private Bid mockBid2;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        auctionService = new AuctionServiceImpl(
                bidRepository,
                auctionRepository,
                eventPublisher,
                walletService,
                orderService,
                orderRepository,
                meterRegistry
        );

        mockBid1 = new Bid();
        mockBid1.setId("1");
        mockBid1.setAuctionId("123");
        mockBid1.setBidderId("user-A");
        mockBid1.setAmount(150.0);
        mockBid1.setTimestamp(LocalDateTime.now());

        mockBid2 = new Bid();
        mockBid2.setId("2");
        mockBid2.setAuctionId("123");
        mockBid2.setBidderId("user-B");
        mockBid2.setAmount(100.0);
        mockBid2.setTimestamp(LocalDateTime.now().minusMinutes(5));
    }

    @Test
    void testGetBiddingHistory() {
        String testAuctionId = "123";
        when(bidRepository.findByAuctionIdOrderByAmountDesc(testAuctionId))
                .thenReturn(Arrays.asList(mockBid1, mockBid2));

        List<BidResponseDTO> result = auctionService.getBiddingHistory(testAuctionId);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("user-A", result.get(0).getBidderId());
        assertEquals(150.0, result.get(0).getAmount());

        assertEquals("user-B", result.get(1).getBidderId());
        assertEquals(100.0, result.get(1).getAmount());

        verify(bidRepository).findByAuctionIdOrderByAmountDesc(testAuctionId);
        assertEquals(1.0, counterValue("bidmart.auction.history.requests"));
    }

    @Test
    void testCreateAuction() {
        CreateAuctionRequestDTO request = new CreateAuctionRequestDTO();
        request.setSellerId("seller-123");
        request.setCatalogueListingId("item-456");
        request.setInitialPrice(50.0);
        request.setReservePrice(200.0);
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(7));

        Auction savedMockAuction = new Auction();
        savedMockAuction.setId("generated-uuid-789");
        savedMockAuction.setSellerId(request.getSellerId());
        savedMockAuction.setCatalogueListingId(request.getCatalogueListingId());
        savedMockAuction.setInitialPrice(request.getInitialPrice());
        savedMockAuction.setReservePrice(request.getReservePrice());
        savedMockAuction.setStartTime(request.getStartTime());
        savedMockAuction.setEndTime(request.getEndTime());
        savedMockAuction.setStage(AuctionStage.DRAFT);
        savedMockAuction.setCurrentHighestBid(0.0);

        when(auctionRepository.save(any(Auction.class))).thenReturn(savedMockAuction);

        AuctionResponseDTO response = auctionService.createAuction(request);

        assertNotNull(response);
        assertEquals("generated-uuid-789", response.getId());
        assertEquals("seller-123", response.getSellerId());

        assertEquals(AuctionStage.DRAFT, response.getStage());
        assertEquals(0.0, response.getCurrentHighestBid());
        assertEquals(null, response.getWinnerId());

        verify(auctionRepository, times(1)).save(any(Auction.class));
        assertEquals(1.0, counterValue("bidmart.auction.created"));
    }

    @Test
    void testPlaceBid_Success() {
        String testAuctionId = "auction-123";

        Auction mockAuction = new Auction();
        mockAuction.setId(testAuctionId);
        mockAuction.setStage(AuctionStage.ACTIVE);
        mockAuction.setCurrentHighestBid(100.0);

        PlaceBidRequestDTO request = new PlaceBidRequestDTO();
        request.setBidderId("buyer-999");
        request.setAmount(110.0);

        when(auctionRepository.findById(testAuctionId)).thenReturn(Optional.of(mockAuction));

        BidResponseDTO response = auctionService.placeBid(testAuctionId, request);

        assertNotNull(response);
        assertEquals("buyer-999", response.getBidderId());
        assertEquals(110.0, response.getAmount());

        assertEquals(110.0, mockAuction.getCurrentHighestBid());

        verify(walletService, times(1)).holdFunds("buyer-999", 110L);
        verify(bidRepository, times(1)).save(any(Bid.class));
        verify(auctionRepository, times(1)).save(mockAuction);

        verify(eventPublisher, times(1)).publishEvent(any(BidPlacedEvent.class));
        assertEquals(1.0, counterValue("bidmart.auction.bid.success"));
        assertEquals(1.0, counterValue("bidmart.auction.events.bid_published"));
    }

    @Test
    void testPlaceBid_ReleasesPreviousHighestBidder() {
        String testAuctionId = "auction-123";

        Auction mockAuction = new Auction();
        mockAuction.setId(testAuctionId);
        mockAuction.setStage(AuctionStage.ACTIVE);
        mockAuction.setCurrentHighestBid(100.0);

        Bid previousHighestBid = new Bid();
        previousHighestBid.setAuctionId(testAuctionId);
        previousHighestBid.setBidderId("buyer-old");
        previousHighestBid.setAmount(100.0);

        PlaceBidRequestDTO request = new PlaceBidRequestDTO();
        request.setBidderId("buyer-new");
        request.setAmount(110.0);

        when(auctionRepository.findById(testAuctionId)).thenReturn(Optional.of(mockAuction));
        when(bidRepository.findByAuctionIdOrderByAmountDesc(testAuctionId)).thenReturn(List.of(previousHighestBid));

        auctionService.placeBid(testAuctionId, request);

        verify(walletService, times(1)).holdFunds("buyer-new", 110L);
        verify(walletService, times(1)).releaseFunds("buyer-old", 100L);
    }

    @Test
    void testPlaceBid_SameLeaderOnlyHoldsIncrement() {
        String testAuctionId = "auction-123";

        Auction mockAuction = new Auction();
        mockAuction.setId(testAuctionId);
        mockAuction.setStage(AuctionStage.ACTIVE);
        mockAuction.setCurrentHighestBid(100.0);

        Bid previousHighestBid = new Bid();
        previousHighestBid.setAuctionId(testAuctionId);
        previousHighestBid.setBidderId("buyer-999");
        previousHighestBid.setAmount(100.0);

        PlaceBidRequestDTO request = new PlaceBidRequestDTO();
        request.setBidderId("buyer-999");
        request.setAmount(110.0);

        when(auctionRepository.findById(testAuctionId)).thenReturn(Optional.of(mockAuction));
        when(bidRepository.findByAuctionIdOrderByAmountDesc(testAuctionId)).thenReturn(List.of(previousHighestBid));

        auctionService.placeBid(testAuctionId, request);

        verify(walletService, times(1)).holdFunds("buyer-999", 10L);
        verify(walletService, times(0)).releaseFunds(any(), any());
    }

    @Test
    void testGetAuctionByListingId_SettlesWonAuctionAndCreatesPaidOrder() {
        String testListingId = "listing-123";
        String testAuctionId = "auction-123";

        Auction mockAuction = new Auction();
        mockAuction.setId(testAuctionId);
        mockAuction.setCatalogueListingId(testListingId);
        mockAuction.setStage(AuctionStage.ACTIVE);
        mockAuction.setReservePrice(100.0);
        mockAuction.setCurrentHighestBid(150.0);
        mockAuction.setEndTime(LocalDateTime.now().minusMinutes(1));

        Bid winningBid = new Bid();
        winningBid.setAuctionId(testAuctionId);
        winningBid.setBidderId("buyer-999");
        winningBid.setAmount(150.0);

        when(auctionRepository.findByCatalogueListingId(testListingId)).thenReturn(Optional.of(mockAuction));
        when(bidRepository.findByAuctionIdOrderByAmountDesc(testAuctionId)).thenReturn(List.of(winningBid));

        AuctionResponseDTO response = auctionService.getAuctionByListingId(testListingId);

        assertEquals(AuctionStage.WON, response.getStage());
        assertEquals("buyer-999", response.getWinnerId());
        verify(orderService, times(1)).createPaidOrder(any());
        verify(auctionRepository, times(1)).save(mockAuction);
    }

    @Test
    void testGetAuctionByListingId_SettlesUnsoldAuctionAndReleasesHeldFunds() {
        String testListingId = "listing-123";
        String testAuctionId = "auction-123";

        Auction mockAuction = new Auction();
        mockAuction.setId(testAuctionId);
        mockAuction.setCatalogueListingId(testListingId);
        mockAuction.setStage(AuctionStage.ACTIVE);
        mockAuction.setReservePrice(200.0);
        mockAuction.setCurrentHighestBid(150.0);
        mockAuction.setEndTime(LocalDateTime.now().minusMinutes(1));

        Bid highestBid = new Bid();
        highestBid.setAuctionId(testAuctionId);
        highestBid.setBidderId("buyer-999");
        highestBid.setAmount(150.0);

        when(auctionRepository.findByCatalogueListingId(testListingId)).thenReturn(Optional.of(mockAuction));
        when(bidRepository.findByAuctionIdOrderByAmountDesc(testAuctionId)).thenReturn(List.of(highestBid));

        AuctionResponseDTO response = auctionService.getAuctionByListingId(testListingId);

        assertEquals(AuctionStage.UNSOLD, response.getStage());
        assertEquals(null, response.getWinnerId());
        verify(walletService, times(1)).releaseFunds("buyer-999", 150L);
        verify(auctionRepository, times(1)).save(mockAuction);
    }

    @Test
    void testPlaceBid_Fail_AuctionClosed() {
        String testAuctionId = "auction-123";

        Auction mockAuction = new Auction();
        mockAuction.setId(testAuctionId);
        mockAuction.setStage(AuctionStage.CLOSED);

        PlaceBidRequestDTO request = new PlaceBidRequestDTO();
        request.setAmount(500.0);

        when(auctionRepository.findById(testAuctionId)).thenReturn(Optional.of(mockAuction));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            auctionService.placeBid(testAuctionId, request);
        });

        assertTrue(exception.getMessage().contains("Bids are not allowed"));

        verify(bidRepository, times(0)).save(any(Bid.class));
        assertEquals(1.0, counterValue("bidmart.auction.bid.failure"));
        assertEquals(1.0, counterValue("bidmart.auction.bid.rejected_closed"));
    }

    @Test
    void testPlaceBid_Fail_BidTooLow() {
        String testAuctionId = "auction-123";

        Auction mockAuction = new Auction();
        mockAuction.setId(testAuctionId);
        mockAuction.setStage(AuctionStage.ACTIVE);
        mockAuction.setCurrentHighestBid(100.0);

        PlaceBidRequestDTO request = new PlaceBidRequestDTO();
        request.setAmount(102.0);

        when(auctionRepository.findById(testAuctionId)).thenReturn(Optional.of(mockAuction));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            auctionService.placeBid(testAuctionId, request);
        });

        assertTrue(exception.getMessage().contains("must be at least"));
        verify(bidRepository, times(0)).save(any(Bid.class));
        assertEquals(1.0, counterValue("bidmart.auction.bid.failure"));
        assertEquals(1.0, counterValue("bidmart.auction.bid.rejected_too_low"));
    }

    private double counterValue(String name) {
        return meterRegistry.get(name).counter().count();
    }
}
