package id.ac.ui.cs.advprog.bidmart.auction.event;

import java.time.LocalDateTime;

public record BidPlacedEvent(
        String auctionId,
        String bidderId,
        Double amount,
        LocalDateTime timestamp
) {}