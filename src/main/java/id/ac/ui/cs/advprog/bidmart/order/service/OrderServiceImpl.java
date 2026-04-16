package id.ac.ui.cs.advprog.bidmart.order.service;

import id.ac.ui.cs.advprog.bidmart.order.dto.CreateOrderRequest;
import id.ac.ui.cs.advprog.bidmart.order.event.NotificationPublisher;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderCompletedEvent;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderCreatedEvent;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderShippedEvent;
import id.ac.ui.cs.advprog.bidmart.order.model.OrderEntity;
import id.ac.ui.cs.advprog.bidmart.order.model.OrderStatus;
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
        if (orderRepository.existsByAuctionId(request.getAuctionId())) {
            throw new IllegalStateException("Order already exists for auction=" + request.getAuctionId());
        }

        OrderEntity order = new OrderEntity(
                request.getAuctionId(),
                request.getBuyerUsername(),
                request.getSellerUsername(),
                request.getShippingAddress()
        );

        OrderEntity savedOrder = orderRepository.save(order);
        notificationPublisher.publish(
                new OrderCreatedEvent(
                        savedOrder.getId(),
                        savedOrder.getAuctionId(),
                        savedOrder.getBuyerUsername(),
                        savedOrder.getSellerUsername()
                )
        );
        return savedOrder;
    }

    @Override
    public List<OrderEntity> findAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public OrderEntity findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found id=" + orderId));
    }

    @Override
    public List<OrderEntity> findByBuyer(String buyerUsername) {
        return orderRepository.findByBuyerUsername(buyerUsername);
    }

    @Override
    public List<OrderEntity> findBySeller(String sellerUsername) {
        return orderRepository.findBySellerUsername(sellerUsername);
    }

    @Override
    public OrderEntity updateStatus(Long orderId, OrderStatus status) {
        OrderEntity order = findById(orderId);

        switch (status) {
            case PAID -> order.markPaid();
            case COMPLETED -> {
                order.markCompleted();
                notificationPublisher.publish(new OrderCompletedEvent(order.getId(), order.getBuyerUsername()));
            }
            case CANCELLED -> order.markCancelled();
            default -> throw new IllegalArgumentException("Unsupported transition target=" + status);
        }

        return orderRepository.save(order);
    }

    @Override
    public OrderEntity markShipped(Long orderId, String trackingNumber) {
        OrderEntity order = findById(orderId);
        order.markShipped(trackingNumber);
        OrderEntity saved = orderRepository.save(order);

        notificationPublisher.publish(new OrderShippedEvent(saved.getId(), saved.getBuyerUsername(), saved.getTrackingNumber()));
        return saved;
    }
}
