package id.ac.ui.cs.advprog.bidmart.auction.controller;

import id.ac.ui.cs.advprog.bidmart.auction.dto.AuctionResponseDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.CreateAuctionRequestDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.BidResponseDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.PlaceBidRequestDTO;
import id.ac.ui.cs.advprog.bidmart.auction.service.AuctionService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @GetMapping("/{auctionId}/bids")
    public ResponseEntity<List<BidResponseDTO>> getBiddingHistory(@PathVariable String auctionId) {
        List<BidResponseDTO> history = auctionService.getBiddingHistory(auctionId);
        return ResponseEntity.ok(history);
    }
    @PostMapping
    public ResponseEntity<AuctionResponseDTO> createAuction(@RequestBody CreateAuctionRequestDTO request) {
        AuctionResponseDTO response = auctionService.createAuction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{auctionId}/bids")
    public ResponseEntity<?> placeBid(
            @PathVariable String auctionId,
            @RequestBody PlaceBidRequestDTO request) {

        try {
            BidResponseDTO response = auctionService.placeBid(auctionId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

