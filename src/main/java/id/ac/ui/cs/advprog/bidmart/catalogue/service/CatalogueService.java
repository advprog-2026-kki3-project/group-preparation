package id.ac.ui.cs.advprog.bidmart.catalogue.service;

import id.ac.ui.cs.advprog.bidmart.catalogue.model.Listing;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CatalogueService {
    List<Listing> searchListings(String keyword, String categoryId, Double minPrice, Double maxPrice, LocalDateTime endDate);
    Listing getListingById(String id);
    Listing createListing(Listing listing);
    Listing updateListing(String id, String description, String imageUrl, UUID sellerId);
    void cancelListing(String id, UUID sellerId);
}