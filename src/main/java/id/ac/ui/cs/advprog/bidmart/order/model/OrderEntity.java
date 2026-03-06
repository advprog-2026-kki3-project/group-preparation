package id.ac.ui.cs.advprog.bidmart.order.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long auctionId;
    private String winnerUsername;
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Instant createdAt;

    protected OrderEntity() {
        // JPA constructor
    }

    public OrderEntity(Long auctionId, String winnerUsername, String shippingAddress) {
        this.auctionId = auctionId;
        this.winnerUsername = winnerUsername;
        this.shippingAddress = shippingAddress;
        this.status = OrderStatus.CREATED;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getAuctionId() {
        return auctionId;
    }

    public String getWinnerUsername() {
        return winnerUsername;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
