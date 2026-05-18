package id.ac.ui.cs.advprog.bidmart.catalogue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bidmart.catalogue.model.Category;
import id.ac.ui.cs.advprog.bidmart.catalogue.model.Listing;
import id.ac.ui.cs.advprog.bidmart.catalogue.service.CatalogueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ListingRestControllerTest {

    private MockMvc mockMvc;

    private CatalogueService catalogueService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        catalogueService = mock(CatalogueService.class);

        ListingRestController controller =
                new ListingRestController(catalogueService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateListing() throws Exception {

        Listing listing = new Listing();

        listing.setTitle("iPhone");

        when(catalogueService.createListing(any(Listing.class)))
                .thenReturn(listing);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("seller-user");

        mockMvc.perform(post("/api/listings")

                        .principal(authentication)

                        .contentType(MediaType.APPLICATION_JSON)

                        .content(objectMapper.writeValueAsString(listing)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.title")
                        .value("iPhone"));

        verify(catalogueService)
                .createListing(any(Listing.class));
    }

    @Test
    void testGetAllListings() throws Exception {

        Listing listing1 = new Listing();

        listing1.setTitle("iPhone");

        Listing listing2 = new Listing();

        listing2.setTitle("Samsung");

        when(catalogueService.findAllListings())
                .thenReturn(List.of(listing1, listing2));

        mockMvc.perform(get("/api/listings"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.length()")
                        .value(2))

                .andExpect(jsonPath("$[0].title")
                        .value("iPhone"))

                .andExpect(jsonPath("$[1].title")
                        .value("Samsung"));

        verify(catalogueService).findAllListings();
    }

    @Test
    void testGetAllListingsKeywordFilter() throws Exception {

        Listing listing = new Listing();

        listing.setTitle("iPhone");

        when(catalogueService.findAllListings())
                .thenReturn(List.of(listing));

        mockMvc.perform(get("/api/listings")
                        .param("keyword", "iphone"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.length()")
                        .value(1));

        verify(catalogueService).findAllListings();
    }

    @Test
    void testGetAllListingsCategoryFilter() throws Exception {

        Category category = new Category();

        category.setName("Electronics");

        Listing listing = new Listing();

        listing.setTitle("Laptop");

        listing.setCategory(category);

        when(catalogueService.findAllListings())
                .thenReturn(List.of(listing));

        mockMvc.perform(get("/api/listings")
                        .param("category", "Electronics"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.length()")
                        .value(1));

        verify(catalogueService).findAllListings();
    }

    @Test
    void testGetAllListingsMinPriceFilter() throws Exception {

        Listing listing = new Listing();

        listing.setCurrentPrice(500.0);

        when(catalogueService.findAllListings())
                .thenReturn(List.of(listing));

        mockMvc.perform(get("/api/listings")
                        .param("minPrice", "100"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.length()")
                        .value(1));

        verify(catalogueService).findAllListings();
    }

    @Test
    void testGetAllListingsMaxPriceFilter() throws Exception {

        Listing listing = new Listing();

        listing.setCurrentPrice(500.0);

        when(catalogueService.findAllListings())
                .thenReturn(List.of(listing));

        mockMvc.perform(get("/api/listings")
                        .param("maxPrice", "1000"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.length()")
                        .value(1));

        verify(catalogueService).findAllListings();
    }

    @Test
    void testGetListingByIdFound() throws Exception {

        Listing listing = new Listing();

        listing.setTitle("MacBook");

        when(catalogueService.getListingById("1"))
                .thenReturn(listing);

        mockMvc.perform(get("/api/listings/1"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.title")
                        .value("MacBook"));

        verify(catalogueService).getListingById("1");
    }

    @Test
    void testGetListingByIdNotFound() throws Exception {

        when(catalogueService.getListingById("1"))
                .thenReturn(null);

        mockMvc.perform(get("/api/listings/1"))

                .andExpect(status().isNotFound());

        verify(catalogueService).getListingById("1");
    }

    @Test
    void testUpdateListing() throws Exception {

        Listing listing = new Listing();

        listing.setTitle("Updated Listing");

        when(catalogueService.updateListing(eq("1"), any(Listing.class)))
                .thenReturn(listing);

        mockMvc.perform(put("/api/listings/1")

                        .contentType(MediaType.APPLICATION_JSON)

                        .content(objectMapper.writeValueAsString(listing)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.title")
                        .value("Updated Listing"));

        verify(catalogueService)
                .updateListing(eq("1"), any(Listing.class));
    }

    @Test
    void testDeleteListing() throws Exception {

        doNothing().when(catalogueService)
                .deleteListing("1");

        mockMvc.perform(delete("/api/listings/1"))

                .andExpect(status().isNoContent());

        verify(catalogueService).deleteListing("1");
    }
}