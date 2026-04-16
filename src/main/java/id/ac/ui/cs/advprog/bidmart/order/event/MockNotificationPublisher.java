package id.ac.ui.cs.advprog.bidmart.order.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class MockNotificationPublisher implements NotificationPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public MockNotificationPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(Object event) {
        applicationEventPublisher.publishEvent(event);
    }
}
