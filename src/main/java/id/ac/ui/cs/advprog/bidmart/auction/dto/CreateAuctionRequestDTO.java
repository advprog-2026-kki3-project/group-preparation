package id.ac.ui.cs.advprog.bidmart.auction.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateAuctionRequestDTO {
    private String sellerId;
    private String catalogueListingId;
    private Double initialPrice;
    private Double reservePrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}