package id.ac.ui.cs.advprog.bidmart.notification.service;

import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationType;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderCompletedEvent;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderCreatedEvent;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderShippedEvent;
import id.ac.ui.cs.advprog.bidmart.auction.event.BidPlacedEvent;
import id.ac.ui.cs.advprog.bidmart.auction.model.Bid;
import id.ac.ui.cs.advprog.bidmart.auction.repository.BidRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class NotificationEventHandler {
    private final NotificationService notificationService;
    private final BidRepository bidRepository;

    public NotificationEventHandler(NotificationService notificationService,
                                    BidRepository bidRepository) {
        this.notificationService = notificationService;
        this.bidRepository = bidRepository;
    }

    @Async
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Handling OrderCreatedEvent orderId={} buyer={}", event.orderId(), event.buyerUsername());

        notificationService.createNotification(
                event.buyerUsername(),
                NotificationType.AUCTION_WON,
                "Congratulations! You won auction #" + event.auctionId(),
                event.orderId(),
                event.auctionId()
        );

        notificationService.createNotification(
                event.buyerUsername(),
                NotificationType.ORDER_CREATED,
                "Your order #" + event.orderId() + " has been created",
                event.orderId(),
                event.auctionId()
        );
    }

    @Async
    @EventListener
    public void onOrderShipped(OrderShippedEvent event) {
        log.info("Handling OrderShippedEvent orderId={} tracking={}", event.orderId(), event.trackingNumber());
        notificationService.createNotification(
                event.buyerUsername(),
                NotificationType.ORDER_SHIPPED,
                "Your order #" + event.orderId() + " was shipped. Tracking: " + event.trackingNumber(),
                event.orderId(),
                null
        );
    }

    @Async
    @EventListener
    public void onOrderCompleted(OrderCompletedEvent event) {
        log.info("Handling OrderCompletedEvent orderId={}", event.orderId());
        notificationService.createNotification(
                event.buyerUsername(),
                NotificationType.ORDER_COMPLETED,
                "Your order #" + event.orderId() + " has been completed",
                event.orderId(),
                null
        );
    }

    @Async
    @EventListener
    public void onBidPlaced(BidPlacedEvent event) {
        log.info("Handling BidPlacedEvent auctionId={} bidder={}", event.auctionId(), event.bidderId());

        notificationService.createNotification(
                event.bidderId(),
                NotificationType.BID_PLACED,
                "Your bid on auction #" + event.auctionId() + " has been placed",
                null,
                event.auctionId()
        );

        List<Bid> bids = bidRepository.findByAuctionIdOrderByAmountDesc(event.auctionId());
        bids.stream()
                .map(Bid::getBidderId)
                .filter(bidderId -> !bidderId.equals(event.bidderId()))
                .findFirst()
                .ifPresent(previousLeader -> notificationService.createNotification(
                        previousLeader,
                        NotificationType.OUTBID,
                        "You have been outbid on auction #" + event.auctionId()
                                + ". New highest bid: $" + event.amount(),
                        null,
                        event.auctionId()
                ));
    }
}