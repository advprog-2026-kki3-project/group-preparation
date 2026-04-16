package id.ac.ui.cs.advprog.bidmart.order.controller;

import id.ac.ui.cs.advprog.bidmart.order.dto.CreateOrderRequest;
import id.ac.ui.cs.advprog.bidmart.order.dto.OrderResponse;
import id.ac.ui.cs.advprog.bidmart.order.dto.ShipOrderRequest;
import id.ac.ui.cs.advprog.bidmart.order.dto.UpdateOrderStatusRequest;
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

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {
        return OrderResponse.fromEntity(orderService.findById(id));
    }

    @GetMapping("/buyer/{buyerUsername}")
    public List<OrderResponse> getByBuyer(@PathVariable String buyerUsername) {
        return orderService.findByBuyer(buyerUsername).stream().map(OrderResponse::fromEntity).toList();
    }

    @GetMapping("/seller/{sellerUsername}")
    public List<OrderResponse> getBySeller(@PathVariable String sellerUsername) {
        return orderService.findBySeller(sellerUsername).stream().map(OrderResponse::fromEntity).toList();
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @RequestBody UpdateOrderStatusRequest request) {
        return OrderResponse.fromEntity(orderService.updateStatus(id, request.getStatus()));
    }

    @PatchMapping("/{id}/shipping")
    public OrderResponse updateShipping(@PathVariable Long id, @RequestBody ShipOrderRequest request) {
        return OrderResponse.fromEntity(orderService.markShipped(id, request.getTrackingNumber()));
    }
}
