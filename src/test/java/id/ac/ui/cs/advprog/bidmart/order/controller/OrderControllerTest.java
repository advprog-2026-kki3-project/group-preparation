package id.ac.ui.cs.advprog.bidmart.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bidmart.order.dto.CreateOrderRequest;
import id.ac.ui.cs.advprog.bidmart.order.dto.ShipOrderRequest;
import id.ac.ui.cs.advprog.bidmart.order.dto.UpdateOrderStatusRequest;
import id.ac.ui.cs.advprog.bidmart.order.model.OrderEntity;
import id.ac.ui.cs.advprog.bidmart.order.model.OrderStatus;
import id.ac.ui.cs.advprog.bidmart.order.service.OrderService;
import id.ac.ui.cs.advprog.bidmart.auth.service.PermissionService;
import id.ac.ui.cs.advprog.bidmart.auth.service.SessionService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private Clock clock;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private SessionService sessionService;

    @Test
    void createOrder_returnsCreatedOrder() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAuctionId("auction-300");
        request.setBuyerUsername("winner-api");
        request.setSellerUsername("seller-api");
        request.setShippingAddress("Shipping API");

        OrderEntity created = new OrderEntity("auction-300", "winner-api", "seller-api", "Shipping API", 0L);
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.auctionId").value("auction-300"))
                .andExpect(jsonPath("$.buyerUsername").value("winner-api"))
                .andExpect(jsonPath("$.sellerUsername").value("seller-api"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getOrders_returnsOrderList() throws Exception {
        when(orderService.findAllOrders()).thenReturn(List.of(new OrderEntity("auction-500", "u1", "s1", "addr", 1000L)));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].auctionId").value("auction-500"))
                .andExpect(jsonPath("$[0].buyerUsername").value("u1"))
                .andExpect(jsonPath("$[0].status").value("CREATED"));
    }

    @Test
    void updateShipping_returnsUpdatedOrder() throws Exception {
        ShipOrderRequest request = new ShipOrderRequest();
        request.setTrackingNumber("TRACK-API");

        OrderEntity shipped = new OrderEntity("auction-10", "buyer", "seller", "addr", 1000L);
        shipped.markPaid();
        shipped.markShipped("TRACK-API");

        when(orderService.markShipped(1L, "TRACK-API")).thenReturn(shipped);

        mockMvc.perform(patch("/api/orders/1/shipping")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingNumber").value("TRACK-API"))
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void updateStatus_returnsUpdatedOrder() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(OrderStatus.CANCELLED);

        OrderEntity cancelled = new OrderEntity("auction-12", "buyer", "seller", "addr", 1000L);
        cancelled.markCancelled();

        when(orderService.updateStatus(2L, OrderStatus.CANCELLED)).thenReturn(cancelled);

        mockMvc.perform(patch("/api/orders/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
