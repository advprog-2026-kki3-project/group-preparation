package id.ac.ui.cs.advprog.bidmart.order.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long auctionId;

    @Column(nullable = false)
    private String buyerUsername;

    @Column(nullable = false)
    private String sellerUsername;

    @Column(nullable = false)
    private String shippingAddress;
    @Column
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    protected OrderEntity() {
        // JPA constructor
    }

    public OrderEntity(Long auctionId, String buyerUsername,String sellerUsername,String shippingAddress) {
        this.auctionId = auctionId;
        this.buyerUsername = buyerUsername;
        this.sellerUsername = sellerUsername;
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

    public String getBuyerUsername() {
        return buyerUsername;
    }

    public String getWinnerUsername() {
        return buyerUsername;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public String getTrackingNumber(){
        return trackingNumber;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markPaid() {
        ensureCurrentStatus(OrderStatus.CREATED);
        this.status = OrderStatus.PAID;
    }

    public void markShipped(String trackingNumber) {
        ensureCurrentStatus(OrderStatus.PAID);
        this.trackingNumber = trackingNumber;
        this.status = OrderStatus.SHIPPED;
    }

    public void markCompleted() {
        ensureCurrentStatus(OrderStatus.SHIPPED);
        this.status = OrderStatus.COMPLETED;
    }

    public void markCancelled() {
        if (this.status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Completed order cannot be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }

    private void ensureCurrentStatus(OrderStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException("Invalid order transition from " + this.status + " to expected " + expected);
        }
    }
}
