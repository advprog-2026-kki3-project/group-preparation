package id.ac.ui.cs.advprog.bidmart.catalogue.service;

import id.ac.ui.cs.advprog.bidmart.catalogue.event.ListingPublishedEvent;
import id.ac.ui.cs.advprog.bidmart.catalogue.model.Listing;
import id.ac.ui.cs.advprog.bidmart.catalogue.model.Category;
import id.ac.ui.cs.advprog.bidmart.catalogue.repository.ListingRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogueServiceImpl implements CatalogueService {

    private final ListingRepository listingRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CatalogueServiceImpl(ListingRepository listingRepository, ApplicationEventPublisher eventPublisher) {
        this.listingRepository = listingRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Listing createListing(Listing listing) {
        listing.setCurrentPrice(listing.getInitialPrice());
        Listing savedListing = listingRepository.save(listing);

        ListingPublishedEvent event = new ListingPublishedEvent(
                savedListing.getId(),
                "seller-admin",
                savedListing.getInitialPrice(),
                savedListing.getReservePrice(),
                savedListing.getStartTime(),
                savedListing.getEndTime()
        );
        eventPublisher.publishEvent(event);

        return savedListing;
    }

    @Override
    public List<Listing> findAllListings() {
        return listingRepository.findAll();
    }

    @Override
    public Listing getListingById(String id) {
        return listingRepository.findById(id).orElse(null);
    }

    @Override
    public Listing updateListing(String id, Listing updated) {
        Listing listing = listingRepository.findById(id).orElseThrow();
        if (listing.getBidCount() > 0) {
            throw new RuntimeException("Cannot update listing with bids");
        }
        listing.setTitle(updated.getTitle());
        listing.setDescription(updated.getDescription());
        return listingRepository.save(listing);
    }

    @Override
    public void deleteListing(String id) {
        Listing listing = listingRepository.findById(id).orElseThrow();
        if (listing.getBidCount() > 0) {
            throw new RuntimeException("Cannot delete listing with bids");
        }
        listingRepository.deleteById(id);
    }

    @Override
    public List<Listing> searchByTitle(String keyword) {
        return listingRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @Override
    public List<Listing> getListingsByCategory(Category category) {
        return listingRepository.findByCategory(category);
    }
}