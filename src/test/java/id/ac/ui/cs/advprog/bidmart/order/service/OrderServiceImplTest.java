package id.ac.ui.cs.advprog.bidmart.order.service;

import id.ac.ui.cs.advprog.bidmart.order.dto.CreateOrderRequest;
import id.ac.ui.cs.advprog.bidmart.order.event.NotificationPublisher;
import id.ac.ui.cs.advprog.bidmart.order.model.OrderEntity;
import id.ac.ui.cs.advprog.bidmart.order.model.OrderStatus;
import id.ac.ui.cs.advprog.bidmart.order.repo.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        request.setAuctionId(101L);
        request.setWinnerUsername("buyer1");
        request.setShippingAddress("UI Street 123");

        OrderEntity persisted = new OrderEntity(101L, "buyer1", "UI Street 123");

        when(orderRepository.save(any(OrderEntity.class))).thenReturn(persisted);

        OrderEntity result = orderService.createOrder(request);

        assertThat(result.getAuctionId()).isEqualTo(101L);
        assertThat(result.getWinnerUsername()).isEqualTo("buyer1");
        assertThat(result.getShippingAddress()).isEqualTo("UI Street 123");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CREATED);

        verify(orderRepository, times(1)).save(any(OrderEntity.class));
        verify(notificationPublisher, times(1)).publishOrderCreated(any());
    }

    @Test
    void findAllOrders_returnsRepositoryData() {
        List<OrderEntity> expected = List.of(new OrderEntity(202L, "buyer2", "Address 2"));
        when(orderRepository.findAll()).thenReturn(expected);

        List<OrderEntity> actual = orderService.findAllOrders();

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst().getAuctionId()).isEqualTo(202L);
        verify(orderRepository).findAll();
    }
}
