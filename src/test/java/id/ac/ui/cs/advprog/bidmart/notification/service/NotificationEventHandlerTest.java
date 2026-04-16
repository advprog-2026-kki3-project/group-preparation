package id.ac.ui.cs.advprog.bidmart.notification.service;

import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationType;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderCompletedEvent;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderCreatedEvent;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderShippedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventHandlerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventHandler notificationEventHandler;

    @Test
    void onOrderCreated_createsNotification() {
        notificationEventHandler.onOrderCreated(new OrderCreatedEvent(1L, 100L, "buyer", "seller"));

        verify(notificationService).createNotification(
                "buyer",
                NotificationType.ORDER_CREATED,
                "Your order #1 has been created",
                1L
        );
    }

    @Test
    void onOrderShipped_createsNotification() {
        notificationEventHandler.onOrderShipped(new OrderShippedEvent(2L, "buyer", "TRACK123"));

        verify(notificationService).createNotification(
                "buyer",
                NotificationType.ORDER_SHIPPED,
                "Your order #2 was shipped. Tracking: TRACK123",
                2L
        );
    }

    @Test
    void onOrderCompleted_createsNotification() {
        notificationEventHandler.onOrderCompleted(new OrderCompletedEvent(3L, "buyer"));

        verify(notificationService).createNotification(
                "buyer",
                NotificationType.ORDER_COMPLETED,
                "Your order #3 has been completed",
                3L
        );
    }
}