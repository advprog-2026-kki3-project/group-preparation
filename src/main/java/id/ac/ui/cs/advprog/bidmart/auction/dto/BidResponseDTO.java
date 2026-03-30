package id.ac.ui.cs.advprog.bidmart.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class BidResponseDTO {
    private String bidderId;
    private Double amount;
    private LocalDateTime timestamp;
}