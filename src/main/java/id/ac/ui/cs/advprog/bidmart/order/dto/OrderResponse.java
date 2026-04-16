package id.ac.ui.cs.advprog.bidmart.order.dto;

import id.ac.ui.cs.advprog.bidmart.order.model.OrderEntity;

public record OrderResponse(
        Long id,
        Long auctionId,
        String buyerUsername,
        String sellerUsername,
        String shippingAddress,
        String trackingNumber,
        String status
) {
    public static OrderResponse fromEntity(OrderEntity entity) {
        return new OrderResponse(
                entity.getId(),
                entity.getAuctionId(),
                entity.getBuyerUsername(),
                entity.getSellerUsername(),
                entity.getShippingAddress(),
                entity.getTrackingNumber(),
                entity.getStatus().name()
        );
    }
}
