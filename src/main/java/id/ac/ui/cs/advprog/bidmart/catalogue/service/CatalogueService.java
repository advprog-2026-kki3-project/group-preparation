package id.ac.ui.cs.advprog.bidmart.catalogue.service;

import id.ac.ui.cs.advprog.bidmart.catalogue.model.Listing;
import java.util.List;

public interface CatalogueService {

    Listing createListing(Listing listing);

    List<Listing> getAllListings();

    Listing getListingById(String id);

    void deleteListing(String id);
}