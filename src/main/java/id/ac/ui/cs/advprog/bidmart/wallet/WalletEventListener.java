package id.ac.ui.cs.advprog.bidmart.wallet;

import id.ac.ui.cs.advprog.bidmart.auction.event.BidPlacedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class WalletEventListener {

    @Autowired
    private WalletService walletService;

    @EventListener
    public void handleBidPlacedEvent(BidPlacedEvent event) {
        Long bidAmount = event.amount().longValue();

        try {
            walletService.holdFunds(event.bidderId(), bidAmount);
            System.out.println("Wallet Module: Successfully held Rp " + bidAmount + " for user " + event.bidderId());
        } catch (IllegalStateException e) {
            System.err.println("Wallet Module: Failed to hold funds - " + e.getMessage());
        }
    }
}