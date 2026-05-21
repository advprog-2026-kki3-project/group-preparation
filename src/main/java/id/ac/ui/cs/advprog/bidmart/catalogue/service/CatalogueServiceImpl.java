package id.ac.ui.cs.advprog.bidmart.catalogue.service;

import id.ac.ui.cs.advprog.bidmart.catalogue.model.Category;
import id.ac.ui.cs.advprog.bidmart.catalogue.model.Listing;
import id.ac.ui.cs.advprog.bidmart.catalogue.repository.CategoryRepository;
import id.ac.ui.cs.advprog.bidmart.catalogue.repository.ListingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CatalogueServiceImpl implements CatalogueService {

    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;

    public CatalogueServiceImpl(ListingRepository listingRepository, CategoryRepository categoryRepository) {
        this.listingRepository = listingRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Listing> searchListings(String keyword, String categoryId, Double minPrice, Double maxPrice, LocalDateTime endDate) {
        Category category = null;
        if (categoryId != null && !categoryId.isBlank()) {
            category = categoryRepository.findById(categoryId).orElse(null);
        }
        return listingRepository.searchListings(keyword, category, minPrice, maxPrice, endDate);
    }

    @Override
    public Listing getListingById(String id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + id));
    }

    @Override
    public Listing createListing(Listing listing) {
        listing.setBidCount(0);
        listing.setCurrentPrice(listing.getInitialPrice());
        listing.setActive(true);
        return listingRepository.save(listing);
    }

    @Override
    public Listing updateListing(String id, String description, String imageUrl, UUID sellerId) {
        Listing listing = getListingById(id);

        if (!listing.getSellerId().equals(sellerId)) {
            throw new SecurityException("You do not have permission to edit this listing.");
        }

        if (listing.getBidCount() > 0) {
            throw new IllegalStateException("Cannot update a listing that already has active bids.");
        }

        listing.setDescription(description);
        listing.setImageUrl(imageUrl);
        return listingRepository.save(listing);
    }

    @Override
    public void cancelListing(String id, UUID sellerId) {
        Listing listing = getListingById(id);

        if (!listing.getSellerId().equals(sellerId)) {
            throw new SecurityException("You do not have permission to cancel this listing.");
        }

        if (listing.getBidCount() > 0) {
            throw new IllegalStateException("Cannot cancel a listing that already has active bids.");
        }

        listing.setActive(false);
        listingRepository.save(listing);
    }
}