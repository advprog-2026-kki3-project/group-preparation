package id.ac.ui.cs.advprog.bidmart.order.service;

import id.ac.ui.cs.advprog.bidmart.order.dto.CreateOrderRequest;
import id.ac.ui.cs.advprog.bidmart.order.event.NotificationPublisher;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderCompletedEvent;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderCreatedEvent;
import id.ac.ui.cs.advprog.bidmart.order.event.OrderShippedEvent;
import id.ac.ui.cs.advprog.bidmart.order.model.OrderEntity;
import id.ac.ui.cs.advprog.bidmart.order.model.OrderStatus;
import id.ac.ui.cs.advprog.bidmart.order.repository.OrderRepository;
import id.ac.ui.cs.advprog.bidmart.wallet.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final NotificationPublisher notificationPublisher;
    private final WalletService walletService;

    public OrderServiceImpl(OrderRepository orderRepository, NotificationPublisher notificationPublisher, WalletService walletService) {
        this.orderRepository = orderRepository;
        this.notificationPublisher = notificationPublisher;
        this.walletService = walletService;
    }

    @Override
    public OrderEntity createOrder(CreateOrderRequest request) {
        validateCreateOrderRequest(request);
        log.info("Creating order for auctionId={} winner={}", request.getAuctionId(), request.getWinnerUsername());

        if (orderRepository.existsByAuctionId(request.getAuctionId())) {
            log.warn("Duplicate order creation rejected for auctionId={}", request.getAuctionId());
            throw new IllegalStateException("Order already exists for auction=" + request.getAuctionId());
        }

        OrderEntity order = new OrderEntity(
                request.getAuctionId(),
                request.getWinnerUsername(),
                request.getSellerUsername(),
                request.getShippingAddress(),
                request.getAmount()
        );

        OrderEntity savedOrder = orderRepository.save(order);
        log.info("Order created id={} auctionId={}", savedOrder.getId(), savedOrder.getAuctionId());
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
        log.debug("Fetching all orders");
        return orderRepository.findAll();
    }

    @Override
    public OrderEntity findById(Long orderId) {
        log.debug("Fetching order by id={}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found id={}", orderId);
                    return new IllegalArgumentException("Order not found id=" + orderId);
                });
    }

    @Override
    public List<OrderEntity> findByBuyer(String buyerUsername) {
        log.debug("Fetching orders for buyer={}", buyerUsername);
        return orderRepository.findByBuyerUsername(buyerUsername);
    }

    @Override
    public List<OrderEntity> findBySeller(String sellerUsername) {
        log.debug("Fetching orders for seller={}", sellerUsername);
        return orderRepository.findBySellerUsername(sellerUsername);
    }

    @Override
    public OrderEntity updateStatus(Long orderId, OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status must be provided");
        }

        OrderEntity order = findById(orderId);
        log.info("Transitioning order id={} from {} to {}", orderId, order.getStatus(), status);

        Object completionEvent = applyStatusTransition(order, status);
        OrderEntity savedOrder = orderRepository.save(order);

        if (completionEvent != null) {
            notificationPublisher.publish(completionEvent);
        }

        return savedOrder;
    }

    private Object applyStatusTransition(OrderEntity order, OrderStatus status) {
        OrderStatus from = order.getStatus();
        switch (status) {
            case PAID -> {
                order.markPaid();
                walletService.commitPayment(order.getBuyerUsername(), order.getAmount());
            }
            case COMPLETED -> {
                order.markCompleted();
                return new OrderCompletedEvent(order.getId(), order.getWinnerUsername());
            }
            case CANCELLED -> {
                order.markCancelled();
                if (from == OrderStatus.CREATED) {
                    walletService.releaseFunds(order.getBuyerUsername(), order.getAmount());
                }
            }
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
        log.info("Marking order id={} as shipped tracking={}", orderId, trackingNumber);
        order.markShipped(trackingNumber);
        OrderEntity saved = orderRepository.save(order);

        notificationPublisher.publish(new OrderShippedEvent(saved.getId(), saved.getBuyerUsername(), saved.getTrackingNumber()));
        return saved;
    }

    private void validateCreateOrderRequest(CreateOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must be provided");
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be a positive value");
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