package id.ac.ui.cs.advprog.bidmart.catalogue.controller;

import id.ac.ui.cs.advprog.bidmart.catalogue.model.Listing;
import id.ac.ui.cs.advprog.bidmart.catalogue.model.Category;
import id.ac.ui.cs.advprog.bidmart.catalogue.service.CatalogueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/listings")
public class ListingController {

    private final CatalogueService catalogueService;

    public ListingController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @PostMapping
    public Listing createListing(@RequestBody Listing listing) {
        return catalogueService.createListing(listing);
    }

    @GetMapping
    public List<Listing> getListings() {
        return catalogueService.findAllListings();
    }

    @GetMapping("/{id}")
    public Listing getListing(@PathVariable String id) {
        return catalogueService.getListingById(id);
    }

    @PutMapping("/{id}")
    public Listing updateListing(@PathVariable String id, @RequestBody Listing listing) {
        return catalogueService.updateListing(id, listing);
    }

    @DeleteMapping("/{id}")
    public void deleteListing(@PathVariable String id) {
        catalogueService.deleteListing(id);
    }

    @GetMapping("/search")
    public List<Listing> searchListings(@RequestParam String keyword) {
        return catalogueService.searchByTitle(keyword);
    }

    @GetMapping("/category")
    public List<Listing> getByCategory(@RequestBody Category category) {
        return catalogueService.getListingsByCategory(category);
    }
}