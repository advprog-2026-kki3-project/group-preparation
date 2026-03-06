package id.ac.ui.cs.advprog.bidmart.order.service;

import id.ac.ui.cs.advprog.bidmart.order.dto.CreateOrderRequest;
import id.ac.ui.cs.advprog.bidmart.order.event.NotificationPublisher;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderCreatedEvent;
import id.ac.ui.cs.advprog.bidmart.order.model.OrderEntity;
import id.ac.ui.cs.advprog.bidmart.order.repo.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final NotificationPublisher notificationPublisher;

    public OrderServiceImpl(OrderRepository orderRepository, NotificationPublisher notificationPublisher) {
        this.orderRepository = orderRepository;
        this.notificationPublisher = notificationPublisher;
    }

    @Override
    public OrderEntity createOrder(CreateOrderRequest request) {
        OrderEntity order = new OrderEntity(
                request.getAuctionId(),
                request.getWinnerUsername(),
                request.getShippingAddress()
        );

        OrderEntity savedOrder = orderRepository.save(order);
        notificationPublisher.publishOrderCreated(
                new OrderCreatedEvent(savedOrder.getId(), savedOrder.getAuctionId(), savedOrder.getWinnerUsername())
        );
        return savedOrder;
    }

    @Override
    public List<OrderEntity> findAllOrders() {
        return orderRepository.findAll();
    }
}
