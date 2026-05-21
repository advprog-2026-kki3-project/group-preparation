package id.ac.ui.cs.advprog.bidmart.notification.service;

import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationType;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderCompletedEvent;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderCreatedEvent;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderShippedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationEventHandler {
    private final NotificationService notificationService;

    public NotificationEventHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Handling OrderCreatedEvent orderId={} buyer={}", event.orderId(), event.buyerUsername());
        notificationService.createNotification(
                event.buyerUsername(),
                NotificationType.ORDER_CREATED,
                "Your order #" + event.orderId() + " has been created",
                event.orderId()
        );
    }

    @EventListener
    public void onOrderShipped(OrderShippedEvent event) {
        log.info("Handling OrderShippedEvent orderId={} tracking={}", event.orderId(), event.trackingNumber());
        notificationService.createNotification(
                event.buyerUsername(),
                NotificationType.ORDER_SHIPPED,
                "Your order #" + event.orderId() + " was shipped. Tracking: " + event.trackingNumber(),
                event.orderId()
        );
    }

    @EventListener
    public void onOrderCompleted(OrderCompletedEvent event) {
        log.info("Handling OrderCompletedEvent orderId={}", event.orderId());
        notificationService.createNotification(
                event.buyerUsername(),
                NotificationType.ORDER_COMPLETED,
                "Your order #" + event.orderId() + " has been completed",
                event.orderId()
        );
    }
}