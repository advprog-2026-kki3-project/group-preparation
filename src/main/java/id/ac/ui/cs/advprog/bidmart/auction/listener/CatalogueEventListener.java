package id.ac.ui.cs.advprog.bidmart.auction.listener;

import id.ac.ui.cs.advprog.bidmart.auction.dto.CreateAuctionRequestDTO;
import id.ac.ui.cs.advprog.bidmart.catalogue.event.ListingPublishedEvent;
import id.ac.ui.cs.advprog.bidmart.auction.service.AuctionService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CatalogueEventListener {

    private final AuctionService auctionService;

    public CatalogueEventListener(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @EventListener
    public void handleListingPublished(ListingPublishedEvent event) {
        System.out.println("--> Received ListingPublishedEvent! Creating Auction for Listing: " + event.listingId());

        CreateAuctionRequestDTO request = new CreateAuctionRequestDTO();
        request.setCatalogueListingId(event.listingId());
        request.setSellerId(event.sellerId());
        request.setInitialPrice(event.initialPrice());
        request.setReservePrice(event.reservePrice());
        request.setStartTime(event.startTime());
        request.setEndTime(event.endTime());

        auctionService.createAuction(request);
    }
}