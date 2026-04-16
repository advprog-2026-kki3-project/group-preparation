package id.ac.ui.cs.advprog.bidmart.notification.service;

import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationType;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderCompletedEvent;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderCreatedEvent;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderShippedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventHandler {
    private final NotificationService notificationService;

    public NotificationEventHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        notificationService.createNotification(
                event.buyerUsername(),
                NotificationType.ORDER_CREATED,
                "Your order #" + event.orderId() + " has been created",
                event.orderId()
        );
    }

    @EventListener
    public void onOrderShipped(OrderShippedEvent event) {
        notificationService.createNotification(
                event.buyerUsername(),
                NotificationType.ORDER_SHIPPED,
                "Your order #" + event.orderId() + " was shipped. Tracking: " + event.trackingNumber(),
                event.orderId()
        );
    }

    @EventListener
    public void onOrderCompleted(OrderCompletedEvent event) {
        notificationService.createNotification(
                event.buyerUsername(),
                NotificationType.ORDER_COMPLETED,
                "Your order #" + event.orderId() + " has been completed",
                event.orderId()
        );
    }
}