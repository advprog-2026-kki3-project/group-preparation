package id.ac.ui.cs.advprog.bidmart.catalogue.service;

import id.ac.ui.cs.advprog.bidmart.catalogue.model.Listing;
import id.ac.ui.cs.advprog.bidmart.catalogue.repository.ListingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogueServiceImpl implements CatalogueService {

    private final ListingRepository listingRepository;

    public CatalogueServiceImpl(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    public Listing createListing(Listing listing) {
        listing.setCurrentPrice(listing.getInitialPrice());
        return listingRepository.save(listing);
    }

    @Override
    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }

    @Override
    public Listing getListingById(String id) {
        return listingRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteListing(String id) {
        listingRepository.deleteById(id);
    }
}
