package id.ac.ui.cs.advprog.bidmart.notification.controller;

import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationEntity;
import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationType;
import id.ac.ui.cs.advprog.bidmart.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void getNotifications_returnsListForUser() throws Exception {
        NotificationEntity entity = new NotificationEntity("buyer-1", NotificationType.ORDER_CREATED, "Your order #1 has been created", 1L);
        when(notificationService.findByUsername("buyer-1")).thenReturn(List.of(entity));

        mockMvc.perform(get("/api/notifications/buyer-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("buyer-1"))
                .andExpect(jsonPath("$[0].type").value("ORDER_CREATED"))
                .andExpect(jsonPath("$[0].message").value("Your order #1 has been created"))
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    void getNotifications_returnsEmptyListWhenNoneExist() throws Exception {
        when(notificationService.findByUsername("ghost")).thenReturn(List.of());

        mockMvc.perform(get("/api/notifications/ghost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void markRead_returnsUpdatedNotification() throws Exception {
        NotificationEntity entity = new NotificationEntity("buyer-1", NotificationType.ORDER_SHIPPED, "shipped", 5L);
        entity.markRead();
        when(notificationService.markAsRead(42L)).thenReturn(entity);

        mockMvc.perform(patch("/api/notifications/42/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true))
                .andExpect(jsonPath("$.type").value("ORDER_SHIPPED"))
                .andExpect(jsonPath("$.orderId").value(5));
    }
}