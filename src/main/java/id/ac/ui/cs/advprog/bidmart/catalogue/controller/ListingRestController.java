package id.ac.ui.cs.advprog.bidmart.catalogue.controller;

import id.ac.ui.cs.advprog.bidmart.catalogue.model.Category;
import id.ac.ui.cs.advprog.bidmart.catalogue.model.Listing;
import id.ac.ui.cs.advprog.bidmart.catalogue.service.CatalogueService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingRestController {

    private final CatalogueService catalogueService;

    public ListingRestController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    // Create listing
    @PostMapping
    public Listing createListing(@RequestBody Listing listing,
                                 Principal principal) {

        listing.setSellerId(principal.getName());

        return catalogueService.createListing(listing);
    }

    // Fetch listings with filters
    @GetMapping
    public List<Listing> getListings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false) Category category
    ) {

        List<Listing> listings = catalogueService.findAllListings();

        // Keyword filter
        if (keyword != null) {
            listings = listings.stream()
                    .filter(l -> l.getTitle().toLowerCase()
                            .contains(keyword.toLowerCase()))
                    .toList();
        }

        // Category filter
        if (category != null) {
            listings = listings.stream()
                    .filter(l -> l.getCategory().equals(category))
                    .toList();
        }

        // Price range filter
        if (minPrice != null && maxPrice != null) {
            listings = listings.stream()
                    .filter(l -> l.getCurrentPrice() >= minPrice
                            && l.getCurrentPrice() <= maxPrice)
                    .toList();
        }

        // Due date filter
        if (endTime != null) {
            listings = listings.stream()
                    .filter(l -> l.getEndTime().isBefore(endTime))
                    .toList();
        }

        return listings;
    }

    // Fetch single listing details
    @GetMapping("/{id}")
    public Listing getListing(@PathVariable String id) {
        return catalogueService.getListingById(id);
    }

    // Update listing
    @PutMapping("/{id}")
    public Listing updateListing(@PathVariable String id,
                                 @RequestBody Listing listing) {

        return catalogueService.updateListing(id, listing);
    }

    // Delete listing
    @DeleteMapping("/{id}")
    public void deleteListing(@PathVariable String id) {
        catalogueService.deleteListing(id);
    }
}