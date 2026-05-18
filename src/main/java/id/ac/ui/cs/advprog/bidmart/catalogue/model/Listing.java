package id.ac.ui.cs.advprog.bidmart.catalogue.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Seller
    private String sellerId;

    // Basic info
    private String title;
    private String description;
    private String imageUrl;

    // Pricing
    private double initialPrice;
    private double currentPrice;
    private double reservePrice;

    // Auction timing
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Status
    private int bidCount = 0;
    private boolean active = true;

    @ManyToOne
    private Category category;
}