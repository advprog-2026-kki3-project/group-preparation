package id.ac.ui.cs.advprog.bidmart.catalogue.event;

import java.time.LocalDateTime;

public record ListingPublishedEvent(
        String listingId,
        String sellerId,
        Double initialPrice,
        Double reservePrice,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}