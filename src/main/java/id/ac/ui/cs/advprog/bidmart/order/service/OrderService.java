package id.ac.ui.cs.advprog.bidmart.order.service;

import id.ac.ui.cs.advprog.bidmart.order.dto.CreateOrderRequest;
import id.ac.ui.cs.advprog.bidmart.order.model.OrderEntity;

import java.util.List;

public interface OrderService {
    OrderEntity createOrder(CreateOrderRequest request);

    List<OrderEntity> findAllOrders();
}
