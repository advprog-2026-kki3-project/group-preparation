package id.ac.ui.cs.advprog.bidmart.order.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockNotificationPublisher implements NotificationPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public MockNotificationPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(Object event) {
        log.info("Publishing event type={} payload={}", event.getClass().getSimpleName(), event);
        applicationEventPublisher.publishEvent(event);
    }
}
