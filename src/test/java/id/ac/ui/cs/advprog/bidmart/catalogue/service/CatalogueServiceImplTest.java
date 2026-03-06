package id.ac.ui.cs.advprog.bidmart.catalogue.service;

import id.ac.ui.cs.advprog.bidmart.catalogue.repository.ListingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class CatalogueServiceImplTest {

    private CatalogueServiceImpl catalogueService;
    private ListingRepository listingRepository;

    @BeforeEach
    void setUp() {
        listingRepository = Mockito.mock(ListingRepository.class);
        catalogueService = new CatalogueServiceImpl(listingRepository);
    }

    @Test
    void testGetAllListings() {
        Mockito.when(listingRepository.findAll()).thenReturn(new ArrayList<>());

        assertNotNull(catalogueService.findAllListings());
    }
}