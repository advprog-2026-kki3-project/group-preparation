package id.ac.ui.cs.advprog.bidmart.catalogue.service;

import id.ac.ui.cs.advprog.bidmart.catalogue.model.Listing;
import id.ac.ui.cs.advprog.bidmart.catalogue.model.Category;

import java.util.List;

public interface CatalogueService {

    Listing createListing(Listing listing);

    List<Listing> findAllListings();

    Listing getListingById(String id);

    Listing updateListing(String id, Listing listing);

    void deleteListing(String id);

    List<Listing> searchByTitle(String keyword);

    List<Listing> getListingsByCategory(Category category);
}