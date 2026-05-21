package id.ac.ui.cs.advprog.bidmart.order.service;

import id.ac.ui.cs.advprog.bidmart.order.dto.CreateOrderRequest;
import id.ac.ui.cs.advprog.bidmart.order.event.NotificationPublisher;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderCompletedEvent;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderCreatedEvent;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderShippedEvent;
import id.ac.ui.cs.advprog.bidmart.order.model.OrderEntity;
import id.ac.ui.cs.advprog.bidmart.order.model.OrderStatus;
import id.ac.ui.cs.advprog.bidmart.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_savesOrderAndPublishesEvent() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAuctionId("auction-101");
        request.setBuyerUsername("buyer1");
        request.setSellerUsername("seller1");
        request.setShippingAddress("UI Street 123");

        OrderEntity persisted = new OrderEntity("auction-101", "buyer1", "seller1", "UI Street 123");

        when(orderRepository.existsByAuctionId("auction-101")).thenReturn(false);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(persisted);

        OrderEntity result = orderService.createOrder(request);

        assertThat(result.getAuctionId()).isEqualTo("auction-101");
        assertThat(result.getBuyerUsername()).isEqualTo("buyer1");
        assertThat(result.getSellerUsername()).isEqualTo("seller1");
        assertThat(result.getShippingAddress()).isEqualTo("UI Street 123");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CREATED);

        verify(orderRepository, times(1)).save(any(OrderEntity.class));
        verify(notificationPublisher, times(1)).publish(argThat(event -> event instanceof OrderCreatedEvent));
    }

    @Test
    void createOrder_rejectsMissingRequiredFields() {
        CreateOrderRequest request = new CreateOrderRequest();

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Auction id must be provided");

        verify(orderRepository, never()).save(any());
        verify(notificationPublisher, never()).publish(any());
    }

    @Test
    void createOrder_preventsDuplicateOrderPerAuction() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAuctionId("auction-777");
        request.setBuyerUsername("b");
        request.setSellerUsername("s");
        request.setShippingAddress("a");

        when(orderRepository.existsByAuctionId("auction-777")).thenReturn(true);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Order already exists");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void findAllOrders_returnsRepositoryData() {
        List<OrderEntity> expected = List.of(new OrderEntity("auction-202", "buyer2","seller2", "Address 2"));
        when(orderRepository.findAll()).thenReturn(expected);

        List<OrderEntity> actual = orderService.findAllOrders();

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst().getAuctionId()).isEqualTo("auction-202");
        verify(orderRepository).findAll();
    }

    @Test
    void updateStatus_supportsLifecycleTransitionUntilCompleted() {
        OrderEntity order = new OrderEntity("auction-501", "buyer", "seller", "Address");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderEntity paid = orderService.updateStatus(1L, OrderStatus.PAID);
        assertThat(paid.getStatus()).isEqualTo(OrderStatus.PAID);

        OrderEntity shipped = orderService.markShipped(1L, "TRACK123");
        assertThat(shipped.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(shipped.getTrackingNumber()).isEqualTo("TRACK123");

        OrderEntity completed = orderService.updateStatus(1L, OrderStatus.COMPLETED);
        assertThat(completed.getStatus()).isEqualTo(OrderStatus.COMPLETED);

        verify(notificationPublisher).publish(argThat(event -> event instanceof OrderShippedEvent));
        verify(notificationPublisher).publish(argThat(event -> event instanceof OrderCompletedEvent));
    }

    @Test
    void updateStatus_rejectsInvalidTransition() {
        OrderEntity order = new OrderEntity("auction-501", "buyer", "seller", "Address");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.COMPLETED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid order transition");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void markShipped_rejectsBlankTrackingNumber() {
        assertThatThrownBy(() -> orderService.markShipped(1L, "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tracking number must be provided");

        verify(orderRepository, never()).save(any());
    }
}