package id.ac.ui.cs.advprog.bidmart.order.event;

public interface NotificationPublisher {
    void publishOrderCreated(OrderCreatedEvent event);
}
