package id.ac.ui.cs.advprog.bidmart.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bidmart.order.dto.CreateOrderRequest;
import id.ac.ui.cs.advprog.bidmart.order.model.OrderEntity;
import id.ac.ui.cs.advprog.bidmart.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrder_returnsCreatedOrder() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAuctionId(300L);
        request.setWinnerUsername("winner-api");
        request.setShippingAddress("Shipping API");

        OrderEntity created = new OrderEntity(300L, "winner-api", "Shipping API");

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.auctionId").value(300L))
                .andExpect(jsonPath("$.winnerUsername").value("winner-api"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getOrders_returnsOrderList() throws Exception {
        when(orderService.findAllOrders()).thenReturn(List.of(new OrderEntity(500L, "u1", "addr")));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].auctionId").value(500L))
                .andExpect(jsonPath("$[0].winnerUsername").value("u1"))
                .andExpect(jsonPath("$[0].status").value("CREATED"));
    }
}
