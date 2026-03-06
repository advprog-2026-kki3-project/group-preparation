package id.ac.ui.cs.advprog.bidmart.order.dto;

import id.ac.ui.cs.advprog.bidmart.order.model.OrderEntity;

public record OrderResponse(
        Long id,
        Long auctionId,
        String winnerUsername,
        String shippingAddress,
        String status
) {
    public static OrderResponse fromEntity(OrderEntity entity) {
        return new OrderResponse(
                entity.getId(),
                entity.getAuctionId(),
                entity.getWinnerUsername(),
                entity.getShippingAddress(),
                entity.getStatus().name()
        );
    }
}
