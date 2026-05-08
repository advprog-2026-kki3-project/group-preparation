package id.ac.ui.cs.advprog.bidmart.catalogue.repository;

import id.ac.ui.cs.advprog.bidmart.catalogue.model.Listing;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ListingRepositoryTest {

    @Autowired
    private ListingRepository repository;

    @Test
    void testSaveAndFindAll() {
        Listing listing = new Listing();
        listing.setTitle("Laptop");
        listing.setDescription("Gaming");
        listing.setInitialPrice(100);
        listing.setCurrentPrice(100);

        repository.save(listing);

        List<Listing> result = repository.findAll();
        assertFalse(result.isEmpty());
    }

    @Test
    void testSearchByTitle() {
        Listing listing = new Listing();
        listing.setTitle("Phone");
        listing.setInitialPrice(200);
        listing.setCurrentPrice(200);

        repository.save(listing);

        List<Listing> result =
                repository.findByTitleContainingIgnoreCase("phone");

        assertFalse(result.isEmpty());
    }
}