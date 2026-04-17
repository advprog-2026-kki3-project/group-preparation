package id.ac.ui.cs.advprog.bidmart.catalogue.controller;

import id.ac.ui.cs.advprog.bidmart.catalogue.service.CatalogueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ListingControllerTest {

    private MockMvc mockMvc;
    private CatalogueService service;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(CatalogueService.class);
        ListingController controller = new ListingController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGetAllListings() throws Exception {
        when(service.findAllListings()).thenReturn(List.of());

        mockMvc.perform(get("/listings"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetListingById() throws Exception {
        when(service.getListingById("1")).thenReturn(null);

        mockMvc.perform(get("/listings/1"))
                .andExpect(status().isOk());
    }
}