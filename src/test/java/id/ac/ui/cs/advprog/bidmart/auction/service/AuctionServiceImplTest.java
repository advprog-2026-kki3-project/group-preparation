package id.ac.ui.cs.advprog.bidmart.auction.service;

import id.ac.ui.cs.advprog.bidmart.auction.model.Auction;
import id.ac.ui.cs.advprog.bidmart.auction.model.AuctionStage;
import id.ac.ui.cs.advprog.bidmart.auction.repository.AuctionRepository;
import id.ac.ui.cs.advprog.bidmart.auction.dto.CreateAuctionRequestDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.AuctionResponseDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.BidResponseDTO;
import id.ac.ui.cs.advprog.bidmart.auction.model.Bid;
import id.ac.ui.cs.advprog.bidmart.auction.repository.BidRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuctionServiceImplTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @InjectMocks
    private AuctionServiceImpl auctionService;

    private Bid mockBid1;
    private Bid mockBid2;

    @BeforeEach
    void setUp() {
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

        verify(auctionRepository, times(1)).save(any(Auction.class));
    }
}