package id.ac.ui.cs.advprog.bidmart.order.service;

import id.ac.ui.cs.advprog.bidmart.order.dto.CreateOrderRequest;
import id.ac.ui.cs.advprog.bidmart.order.model.OrderEntity;
import id.ac.ui.cs.advprog.bidmart.order.model.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderEntity createOrder(CreateOrderRequest request);

    List<OrderEntity> findAllOrders();

    OrderEntity findById(Long orderId);

    List<OrderEntity> findByBuyer(String buyerUsername);

    List<OrderEntity> findBySeller(String sellerUsername);

    OrderEntity updateStatus(Long orderId, OrderStatus status);

    OrderEntity markShipped(Long orderId, String trackingNumber);
}
