package id.ac.ui.cs.advprog.bidmart.auction.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceBidRequestDTO {
    private String bidderId;
    private Double amount;
}