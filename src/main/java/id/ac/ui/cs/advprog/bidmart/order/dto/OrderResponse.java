package id.ac.ui.cs.advprog.bidmart.order.dto;

import id.ac.ui.cs.advprog.bidmart.order.model.OrderEntity;

public record OrderResponse(
        Long id,
        String auctionId,
        String winnerUsername,
        String buyerUsername,
        String sellerUsername,
        String shippingAddress,
        String trackingNumber,
        Long amount,
        String status
) {
    public static OrderResponse fromEntity(OrderEntity entity) {
        return new OrderResponse(
                entity.getId(),
                entity.getAuctionId(),
                entity.getWinnerUsername(),
                entity.getBuyerUsername(),
                entity.getSellerUsername(),
                entity.getShippingAddress(),
                entity.getTrackingNumber(),
                entity.getAmount(),
                entity.getStatus().name()
        );
    }
}
