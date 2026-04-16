package id.ac.ui.cs.advprog.bidmart.notification.dto;

import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationEntity;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String username,
        String type,
        String message,
        Long orderId,
        boolean read,
        Instant createdAt
) {
    public static NotificationResponse fromEntity(NotificationEntity entity) {
        return new NotificationResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getType().name(),
                entity.getMessage(),
                entity.getOrderId(),
                entity.isRead(),
                entity.getCreatedAt()
        );
    }
}