package id.ac.ui.cs.advprog.bidmart.order.controller;

import id.ac.ui.cs.advprog.bidmart.order.dto.CreateOrderRequest;
import id.ac.ui.cs.advprog.bidmart.order.dto.OrderResponse;
import id.ac.ui.cs.advprog.bidmart.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@RequestBody CreateOrderRequest request) {
        return OrderResponse.fromEntity(orderService.createOrder(request));
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.findAllOrders().stream().map(OrderResponse::fromEntity).toList();
    }
}
