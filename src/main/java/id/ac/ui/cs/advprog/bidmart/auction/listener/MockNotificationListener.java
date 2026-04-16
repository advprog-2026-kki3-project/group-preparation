package id.ac.ui.cs.advprog.bidmart.auction.listener;

import id.ac.ui.cs.advprog.bidmart.auction.event.BidPlacedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MockNotificationListener {
    @EventListener
    public void handleBidPlacedEvent(BidPlacedEvent event) {
        System.out.println("\n====== [MOCK NOTIFICATION MODULE] ======");
        System.out.println("EVENT RECEIVED: New Bid Placed!");
        System.out.println("   Auction ID: " + event.auctionId());
        System.out.println("   Bidder ID:  " + event.bidderId());
        System.out.println("   Amount:     $" + event.amount());
        System.out.println("========================================\n");
    }
}