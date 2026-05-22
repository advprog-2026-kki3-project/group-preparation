package id.ac.ui.cs.advprog.bidmart.notification.controller;

import id.ac.ui.cs.advprog.bidmart.notification.dto.NotificationResponse;
import id.ac.ui.cs.advprog.bidmart.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notifications", description = "User notification endpoints")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "List notifications for a user, newest first")
    @GetMapping("/{username}")
    public List<NotificationResponse> getNotifications(@PathVariable String username) {
        return notificationService.findByUsername(username).stream().map(NotificationResponse::fromEntity).toList();
    }

    @Operation(summary = "Mark a notification as read")
    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markRead(@PathVariable Long notificationId) {
        return NotificationResponse.fromEntity(notificationService.markAsRead(notificationId));
    }
}