package id.ac.ui.cs.advprog.bidmart.auction.controller;

import id.ac.ui.cs.advprog.bidmart.auction.dto.AuctionResponseDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.BidResponseDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.CreateAuctionRequestDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.PlaceBidRequestDTO;
import id.ac.ui.cs.advprog.bidmart.auction.repository.AuctionRepository;
import id.ac.ui.cs.advprog.bidmart.auction.service.AuctionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auctionService;
    private final AuctionRepository auctionRepository;

    public AuctionController(AuctionService auctionService, AuctionRepository auctionRepository) {
        this.auctionService = auctionService;
        this.auctionRepository = auctionRepository;
    }

    @GetMapping("/{auctionId}/bids")
    public ResponseEntity<List<BidResponseDTO>> getBiddingHistory(@PathVariable String auctionId) {
        List<BidResponseDTO> history = auctionService.getBiddingHistory(auctionId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/{auctionId}/bids")
    public ResponseEntity<?> placeBid(
            @PathVariable String auctionId,
            @RequestBody PlaceBidRequestDTO request,
            Principal principal) {
        try {
            request.setBidderId(principal.getName());
            BidResponseDTO response = auctionService.placeBid(auctionId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<AuctionResponseDTO> createAuction(@RequestBody CreateAuctionRequestDTO request) {
        AuctionResponseDTO response = auctionService.createAuction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/listing/{listingId}")
    public ResponseEntity<?> getAuctionByListingId(@PathVariable String listingId) {
        return auctionRepository.findByCatalogueListingId(listingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{auctionId}/finalize")
    public ResponseEntity<?> finalizeAuction(@PathVariable String auctionId) {
        try {
            AuctionResponseDTO response = auctionService.finalizeAuction(auctionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}