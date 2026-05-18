package id.ac.ui.cs.advprog.bidmart.catalogue.controller;

import id.ac.ui.cs.advprog.bidmart.catalogue.model.Listing;
import id.ac.ui.cs.advprog.bidmart.catalogue.service.CatalogueService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingRestController {

    private final CatalogueService catalogueService;

    public ListingRestController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    // CREATE LISTING
    @PostMapping
    public ResponseEntity<Listing> createListing(
            @RequestBody Listing listing,
            Authentication authentication
    ) {

        // Extract sellerId securely from JWT Principal
        String sellerId = authentication.getName();

        listing.setSellerId(sellerId);

        return ResponseEntity.ok(
                catalogueService.createListing(listing)
        );
    }

    // GET ALL LISTINGS + FILTERS
    @GetMapping
    public ResponseEntity<List<Listing>> getAllListings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {

        List<Listing> listings = catalogueService.findAllListings();

        // keyword filter
        if (keyword != null && !keyword.isBlank()) {

            listings = listings.stream()
                    .filter(listing ->
                            listing.getTitle().toLowerCase()
                                    .contains(keyword.toLowerCase())
                    )
                    .toList();
        }

        // category filter
        if (category != null && !category.isBlank()) {

            listings = listings.stream()
                    .filter(listing ->
                            listing.getCategory() != null &&
                                    listing.getCategory().getName()
                                            .equalsIgnoreCase(category)
                    )
                    .toList();
        }

        // min price filter
        if (minPrice != null) {

            listings = listings.stream()
                    .filter(listing ->
                            listing.getCurrentPrice() >= minPrice
                    )
                    .toList();
        }

        // max price filter
        if (maxPrice != null) {

            listings = listings.stream()
                    .filter(listing ->
                            listing.getCurrentPrice() <= maxPrice
                    )
                    .toList();
        }

        return ResponseEntity.ok(listings);
    }

    // GET SINGLE LISTING
    @GetMapping("/{id}")
    public ResponseEntity<Listing> getListingById(
            @PathVariable String id
    ) {

        Listing listing = catalogueService.getListingById(id);

        if (listing == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(listing);
    }

    // UPDATE LISTING
    @PutMapping("/{id}")
    public ResponseEntity<Listing> updateListing(
            @PathVariable String id,
            @RequestBody Listing listing
    ) {

        return ResponseEntity.ok(
                catalogueService.updateListing(id, listing)
        );
    }

    // DELETE LISTING
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(
            @PathVariable String id
    ) {

        catalogueService.deleteListing(id);

        return ResponseEntity.noContent().build();
    }
}