package id.ac.ui.cs.advprog.bidmart.catalogue.service;

import id.ac.ui.cs.advprog.bidmart.catalogue.model.Listing;
import id.ac.ui.cs.advprog.bidmart.catalogue.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CatalogueServiceImplTest {

    private ListingRepository repository;
    private CatalogueServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(ListingRepository.class);
        service = new CatalogueServiceImpl(repository);
    }

    @Test
    void testCreateListing() {
        Listing listing = new Listing();
        listing.setInitialPrice(100);

        when(repository.save(any())).thenReturn(listing);

        Listing result = service.createListing(listing);

        assertEquals(100, result.getCurrentPrice());
        verify(repository).save(listing);
    }

    @Test
    void testFindAllListings() {
        when(repository.findAll()).thenReturn(List.of(new Listing()));

        List<Listing> result = service.findAllListings();

        assertEquals(1, result.size());
    }

    @Test
    void testGetListingById() {
        Listing listing = new Listing();
        when(repository.findById("1")).thenReturn(Optional.of(listing));

        Listing result = service.getListingById("1");

        assertNotNull(result);
    }

    @Test
    void testDeleteListing_noBids() {
        Listing listing = new Listing();
        listing.setBidCount(0);

        when(repository.findById("1")).thenReturn(Optional.of(listing));

        service.deleteListing("1");

        verify(repository).deleteById("1");
    }

    @Test
    void testDeleteListing_withBids_shouldThrow() {
        Listing listing = new Listing();
        listing.setBidCount(2);

        when(repository.findById("1")).thenReturn(Optional.of(listing));

        assertThrows(RuntimeException.class, () -> {
            service.deleteListing("1");
        });
    }
}