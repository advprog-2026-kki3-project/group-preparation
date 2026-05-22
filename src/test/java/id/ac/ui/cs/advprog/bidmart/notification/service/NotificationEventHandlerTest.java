package id.ac.ui.cs.advprog.bidmart.notification.service;

import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationType;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderCompletedEvent;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderCreatedEvent;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderShippedEvent;
import id.ac.ui.cs.advprog.bidmart.auction.event.BidPlacedEvent;
import id.ac.ui.cs.advprog.bidmart.auction.model.Bid;
import id.ac.ui.cs.advprog.bidmart.auction.repository.BidRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationEventHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private BidRepository bidRepository;

    @InjectMocks
    private NotificationEventHandler notificationEventHandler;

    @Test
    void onOrderCreated_createsNotification() {
        notificationEventHandler.onOrderCreated(new OrderCreatedEvent(1L, "auction-100", "buyer", "seller"));

        verify(notificationService).createNotification(
                eq("buyer"), eq(NotificationType.AUCTION_WON), anyString(), eq(1L), eq("auction-100"));

        verify(notificationService).createNotification(
                "buyer",
                NotificationType.ORDER_CREATED,
                "Your order #1 has been created",
                1L,
                "auction-100");
    }

    @Test
    void onOrderShipped_createsNotification() {
        notificationEventHandler.onOrderShipped(new OrderShippedEvent(2L, "buyer", "TRACK123"));

        verify(notificationService).createNotification(
                "buyer",
                NotificationType.ORDER_SHIPPED,
                "Your order #2 was shipped. Tracking: TRACK123",
                2L,
                null
        );
    }

    @Test
    void onOrderCompleted_createsNotification() {
        notificationEventHandler.onOrderCompleted(new OrderCompletedEvent(3L, "buyer"));

        verify(notificationService).createNotification(
                "buyer",
                NotificationType.ORDER_COMPLETED,
                "Your order #3 has been completed",
                3L,
                null
        );
    }

    @Test
    void onBidPlaced_createsNotification() {
        notificationEventHandler.onBidPlaced(new BidPlacedEvent("auction-1", "alice", 5000.0, LocalDateTime.now()));
        verify(notificationService).createNotification(
                eq("alice"), eq(NotificationType.BID_PLACED), anyString(), isNull(), eq("auction-1"));
    }

    @Test
    void onBidPlaced_notifiesPreviousLeaderWithOutbid() {
        when(bidRepository.findByAuctionIdOrderByAmountDesc("auction-1"))
                .thenReturn(List.of(
                        bidOf("alice", 5000.0),
                        bidOf("bob", 4000.0)));

        notificationEventHandler.onBidPlaced(new BidPlacedEvent("auction-1", "alice", 5000.0, LocalDateTime.now()));

        verify(notificationService).createNotification(
                eq("alice"), eq(NotificationType.BID_PLACED), anyString(), isNull(), eq("auction-1"));
        verify(notificationService).createNotification(
                eq("bob"), eq(NotificationType.OUTBID), anyString(), isNull(), eq("auction-1"));
    }

    private Bid bidOf(String bidderId, double amount) {
        Bid bid = new Bid();
        bid.setBidderId(bidderId);
        bid.setAmount(amount);
        return bid;
    }
}