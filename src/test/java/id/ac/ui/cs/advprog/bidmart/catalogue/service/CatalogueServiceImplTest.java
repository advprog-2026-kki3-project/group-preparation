package id.ac.ui.cs.advprog.bidmart.catalogue.service;

import id.ac.ui.cs.advprog.bidmart.catalogue.model.Category;
import id.ac.ui.cs.advprog.bidmart.catalogue.model.Listing;
import id.ac.ui.cs.advprog.bidmart.catalogue.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CatalogueServiceImplTest {

    private ListingRepository listingRepository;

    private ApplicationEventPublisher eventPublisher;

    private CatalogueServiceImpl catalogueService;

    @BeforeEach
    void setUp() {

        listingRepository = mock(ListingRepository.class);

        eventPublisher = mock(ApplicationEventPublisher.class);

        catalogueService = new CatalogueServiceImpl(
                listingRepository,
                eventPublisher
        );
    }

    @Test
    void testCreateListing() {

        Listing listing = new Listing();

        listing.setInitialPrice(100.0);
        listing.setReservePrice(200.0);
        listing.setStartTime(LocalDateTime.now());
        listing.setEndTime(LocalDateTime.now().plusDays(1));

        when(listingRepository.save(any(Listing.class)))
                .thenReturn(listing);

        Listing result = catalogueService.createListing(listing);

        assertEquals(100.0, result.getCurrentPrice());

        verify(listingRepository).save(listing);

        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void testFindAllListings() {

        when(listingRepository.findAll())
                .thenReturn(List.of(new Listing(), new Listing()));

        List<Listing> listings = catalogueService.findAllListings();

        assertEquals(2, listings.size());

        verify(listingRepository).findAll();
    }

    @Test
    void testGetListingByIdFound() {

        Listing listing = new Listing();

        when(listingRepository.findById("1"))
                .thenReturn(Optional.of(listing));

        Listing result = catalogueService.getListingById("1");

        assertNotNull(result);

        verify(listingRepository).findById("1");
    }

    @Test
    void testGetListingByIdNotFound() {

        when(listingRepository.findById("1"))
                .thenReturn(Optional.empty());

        Listing result = catalogueService.getListingById("1");

        assertNull(result);
    }

    @Test
    void testUpdateListingSuccess() {

        Category category = new Category();

        Listing existing = new Listing();

        existing.setBidCount(0);

        Listing updated = new Listing();

        updated.setTitle("Updated Title");
        updated.setDescription("Updated Description");
        updated.setImageUrl("image.jpg");
        updated.setReservePrice(500.0);
        updated.setCategory(category);

        when(listingRepository.findById("1"))
                .thenReturn(Optional.of(existing));

        when(listingRepository.save(any(Listing.class)))
                .thenReturn(existing);

        Listing result = catalogueService.updateListing("1", updated);

        assertEquals("Updated Title", result.getTitle());

        assertEquals("Updated Description", result.getDescription());

        assertEquals("image.jpg", result.getImageUrl());

        assertEquals(500.0, result.getReservePrice());

        assertEquals(category, result.getCategory());

        verify(listingRepository).save(existing);
    }

    @Test
    void testUpdateListingWithBidsShouldThrow() {

        Listing listing = new Listing();

        listing.setBidCount(2);

        when(listingRepository.findById("1"))
                .thenReturn(Optional.of(listing));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> catalogueService.updateListing("1", new Listing())
        );

        assertEquals(
                "Cannot update listing with bids",
                exception.getMessage()
        );
    }

    @Test
    void testDeleteListingSuccess() {

        Listing listing = new Listing();

        listing.setBidCount(0);

        when(listingRepository.findById("1"))
                .thenReturn(Optional.of(listing));

        catalogueService.deleteListing("1");

        verify(listingRepository).deleteById("1");
    }

    @Test
    void testDeleteListingWithBidsShouldThrow() {

        Listing listing = new Listing();

        listing.setBidCount(3);

        when(listingRepository.findById("1"))
                .thenReturn(Optional.of(listing));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> catalogueService.deleteListing("1")
        );

        assertEquals(
                "Cannot delete listing with bids",
                exception.getMessage()
        );
    }

    @Test
    void testSearchByTitle() {

        when(listingRepository.findByTitleContainingIgnoreCase("phone"))
                .thenReturn(List.of(new Listing()));

        List<Listing> result =
                catalogueService.searchByTitle("phone");

        assertEquals(1, result.size());

        verify(listingRepository)
                .findByTitleContainingIgnoreCase("phone");
    }

    @Test
    void testGetListingsByCategory() {

        Category category = new Category();

        when(listingRepository.findByCategory(category))
                .thenReturn(List.of(new Listing()));

        List<Listing> result =
                catalogueService.getListingsByCategory(category);

        assertEquals(1, result.size());

        verify(listingRepository).findByCategory(category);
    }

    @Test
    void testProcessBidSuccess() {

        Listing listing = new Listing();

        listing.setActive(true);

        listing.setCurrentPrice(100.0);

        listing.setBidCount(0);

        when(listingRepository.findById("1"))
                .thenReturn(Optional.of(listing));

        catalogueService.processBid("1", 150.0);

        assertEquals(150.0, listing.getCurrentPrice());

        assertEquals(1, listing.getBidCount());

        verify(listingRepository).save(listing);
    }

    @Test
    void testProcessBidInactiveListingShouldThrow() {

        Listing listing = new Listing();

        listing.setActive(false);

        when(listingRepository.findById("1"))
                .thenReturn(Optional.of(listing));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> catalogueService.processBid("1", 150.0)
        );

        assertEquals(
                "Listing is inactive",
                exception.getMessage()
        );
    }

    @Test
    void testProcessBidLowerThanCurrentPriceShouldThrow() {

        Listing listing = new Listing();

        listing.setActive(true);

        listing.setCurrentPrice(200.0);

        when(listingRepository.findById("1"))
                .thenReturn(Optional.of(listing));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> catalogueService.processBid("1", 150.0)
        );

        assertEquals(
                "Bid must be higher than current price",
                exception.getMessage()
        );
    }
}