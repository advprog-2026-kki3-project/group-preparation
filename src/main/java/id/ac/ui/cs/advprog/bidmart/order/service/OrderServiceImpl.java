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
import org.springframework.util.StringUtils;

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
        validateCreateOrderRequest(request);

        if (orderRepository.existsByAuctionId(request.getAuctionId())) {
            throw new IllegalStateException("Order already exists for auction=" + request.getAuctionId());
        }

        OrderEntity order = new OrderEntity(
                request.getAuctionId(),
                request.getWinnerUsername(),
                request.getSellerUsername(),
                request.getShippingAddress()
        );

        OrderEntity savedOrder = orderRepository.save(order);
        notificationPublisher.publish(
                new OrderCreatedEvent(
                        savedOrder.getId(),
                        savedOrder.getAuctionId(),
                        savedOrder.getWinnerUsername(),
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
        if (status == null) {
            throw new IllegalArgumentException("Status must be provided");
        }

        OrderEntity order = findById(orderId);

        Object completionEvent = applyStatusTransition(order, status);
        OrderEntity savedOrder = orderRepository.save(order);

        if (completionEvent != null) {
            notificationPublisher.publish(completionEvent);
        }

        return savedOrder;
    }

    private Object applyStatusTransition(OrderEntity order, OrderStatus status) {
        switch (status) {
            case PAID -> order.markPaid();
            case COMPLETED -> {
                order.markCompleted();
                return new OrderCompletedEvent(order.getId(), order.getWinnerUsername());
            }
            case CANCELLED -> order.markCancelled();
            default -> throw new IllegalArgumentException("Unsupported transition target=" + status);
        }

        return null;
    }

    @Override
    public OrderEntity markShipped(Long orderId, String trackingNumber) {
        if (!StringUtils.hasText(trackingNumber)) {
            throw new IllegalArgumentException("Tracking number must be provided");
        }

        OrderEntity order = findById(orderId);
        order.markShipped(trackingNumber);
        OrderEntity saved = orderRepository.save(order);

        notificationPublisher.publish(new OrderShippedEvent(saved.getId(), saved.getBuyerUsername(), saved.getTrackingNumber()));
        return saved;
    }

    private void validateCreateOrderRequest(CreateOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must be provided");
        }
        if (!StringUtils.hasText(request.getAuctionId())) {
            throw new IllegalArgumentException("Auction id must be provided");
        }
        if (!StringUtils.hasText(request.getWinnerUsername())) {
            throw new IllegalArgumentException("Winner username must be provided");
        }
        if (!StringUtils.hasText(request.getSellerUsername())) {
            throw new IllegalArgumentException("Seller username must be provided");
        }
        if (!StringUtils.hasText(request.getShippingAddress())) {
            throw new IllegalArgumentException("Shipping address must be provided");
        }
    }
}