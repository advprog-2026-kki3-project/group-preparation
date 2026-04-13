package id.ac.ui.cs.advprog.bidmart.auction.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "auctions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String sellerId;
    private String catalogueListingId;

    private Double initialPrice;
    private Double reservePrice;

    private Double currentHighestBid = 0.0;

    // Timeline
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // State Machine
    @Enumerated(EnumType.STRING)
    private AuctionStage stage = AuctionStage.DRAFT;

    public boolean isAcceptingBids() {
        return this.stage == AuctionStage.ACTIVE || this.stage == AuctionStage.EXTENDED;
    }
}