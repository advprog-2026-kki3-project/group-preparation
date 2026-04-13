package id.ac.ui.cs.advprog.bidmart.auction.dto;

import id.ac.ui.cs.advprog.bidmart.auction.model.AuctionStage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AuctionResponseDTO {
    private String id;
    private String sellerId;
    private String catalogueListingId;
    private Double initialPrice;
    private Double reservePrice;
    private Double currentHighestBid;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStage stage;
}