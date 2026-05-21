package id.ac.ui.cs.advprog.bidmart.catalogue.controller;

import id.ac.ui.cs.advprog.bidmart.catalogue.model.Listing;
import id.ac.ui.cs.advprog.bidmart.catalogue.service.CatalogueService;
import id.ac.ui.cs.advprog.bidmart.auth.security.RequiresPermission;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
public class ListingRestController {

    private final CatalogueService catalogueService;

    public ListingRestController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @GetMapping
    public ResponseEntity<List<Listing>> searchListings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(catalogueService.searchListings(keyword, categoryId, minPrice, maxPrice, endDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Listing> getListingById(@PathVariable String id) {
        return ResponseEntity.ok(catalogueService.getListingById(id));
    }

    @PostMapping
    @RequiresPermission(allowed = "catalogue:create")
    public ResponseEntity<Listing> createListing(@RequestBody Listing listing, Principal principal) {
        UUID sellerId = UUID.fromString(principal.getName());
        listing.setSellerId(sellerId);
        return ResponseEntity.ok(catalogueService.createListing(listing));
    }

    @PutMapping("/{id}")
    @RequiresPermission(allowed = "catalogue:update")
    public ResponseEntity<Listing> updateListing(
            @PathVariable String id,
            @RequestBody Listing updateRequest,
            Principal principal) {

        UUID sellerId = UUID.fromString(principal.getName());
        Listing updatedListing = catalogueService.updateListing(
                id,
                updateRequest.getDescription(),
                updateRequest.getImageUrl(),
                sellerId
        );
        return ResponseEntity.ok(updatedListing);
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(allowed = "catalogue:delete")
    public ResponseEntity<Void> cancelListing(@PathVariable String id, Principal principal) {
        UUID sellerId = UUID.fromString(principal.getName());
        catalogueService.cancelListing(id, sellerId);
        return ResponseEntity.noContent().build();
    }
}