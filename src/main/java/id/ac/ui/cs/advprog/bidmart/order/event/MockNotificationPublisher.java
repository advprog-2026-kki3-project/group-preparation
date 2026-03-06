package id.ac.ui.cs.advprog.bidmart.order.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MockNotificationPublisher implements NotificationPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockNotificationPublisher.class);

    @Override
    public void publishOrderCreated(OrderCreatedEvent event) {
        LOGGER.info("Mock notification sent for orderId={} auctionId={} winner={}",
                event.orderId(), event.auctionId(), event.winnerUsername());
    }
}
