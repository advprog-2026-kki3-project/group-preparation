package id.ac.ui.cs.advprog.bidmart.integration;

import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationEntity;
import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationType;
import id.ac.ui.cs.advprog.bidmart.notification.repository.NotificationRepository;
import id.ac.ui.cs.advprog.bidmart.order.dto.CreateOrderRequest;
import id.ac.ui.cs.advprog.bidmart.order.model.OrderEntity;
import id.ac.ui.cs.advprog.bidmart.order.model.OrderStatus;
import id.ac.ui.cs.advprog.bidmart.order.repository.OrderRepository;
import id.ac.ui.cs.advprog.bidmart.order.service.OrderService;
import id.ac.ui.cs.advprog.bidmart.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.List;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(OrderNotificationIntegrationTest.SyncAsyncConfig.class)
class OrderNotificationIntegrationTest {

    @TestConfiguration
    static class SyncAsyncConfig implements AsyncConfigurer {
        @Override
        public Executor getAsyncExecutor() {
            return new SyncTaskExecutor();
        }
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockBean
    private WalletService walletService;

    @Test
    void createOrder_triggersEventAndPersistsBuyerNotification() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAuctionId("integration-auction-1");
        request.setWinnerUsername("integration-buyer");
        request.setSellerUsername("integration-seller");
        request.setShippingAddress("Integration Address");
        request.setAmount(1000L);

        OrderEntity created = orderService.createOrder(request);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(orderRepository.findById(created.getId())).isPresent();

        List<NotificationEntity> notifications =
                notificationRepository.findByUsernameOrderByCreatedAtDesc("integration-buyer");
        assertThat(notifications).extracting(NotificationEntity::getType)
                .containsExactlyInAnyOrder(NotificationType.AUCTION_WON, NotificationType.ORDER_CREATED);
        assertThat(notifications).allMatch(notification -> notification.getOrderId().equals(created.getId()));
    }

    @Test
    void fullLifecycle_emitsNotificationForEachTransition() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAuctionId("integration-auction-2");
        request.setWinnerUsername("lifecycle-buyer");
        request.setSellerUsername("lifecycle-seller");
        request.setShippingAddress("Address");
        request.setAmount(1000L);

        OrderEntity created = orderService.createOrder(request);
        orderService.updateStatus(created.getId(), OrderStatus.PAID);
        orderService.markShipped(created.getId(), "TRACK-INT-1");
        orderService.updateStatus(created.getId(), OrderStatus.COMPLETED);

        List<NotificationEntity> notifications =
                notificationRepository.findByUsernameOrderByCreatedAtDesc("lifecycle-buyer");
        assertThat(notifications).extracting(NotificationEntity::getType)
                .containsExactlyInAnyOrder(
                        NotificationType.AUCTION_WON,
                        NotificationType.ORDER_CREATED,
                        NotificationType.ORDER_SHIPPED,
                        NotificationType.ORDER_COMPLETED
                );
    }
}
