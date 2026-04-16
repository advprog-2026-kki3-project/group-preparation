package id.ac.ui.cs.advprog.bidmart.notification.controller;

import id.ac.ui.cs.advprog.bidmart.notification.dto.NotificationResponse;
import id.ac.ui.cs.advprog.bidmart.notification.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{username}")
    public List<NotificationResponse> getNotifications(@PathVariable String username) {
        return notificationService.findByUsername(username).stream().map(NotificationResponse::fromEntity).toList();
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markRead(@PathVariable Long notificationId) {
        return NotificationResponse.fromEntity(notificationService.markAsRead(notificationId));
    }
}