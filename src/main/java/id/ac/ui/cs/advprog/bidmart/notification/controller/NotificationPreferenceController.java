package id.ac.ui.cs.advprog.bidmart.notification.controller;

import id.ac.ui.cs.advprog.bidmart.notification.dto.NotificationPreferenceRequest;
import id.ac.ui.cs.advprog.bidmart.notification.dto.NotificationPreferenceResponse;
import id.ac.ui.cs.advprog.bidmart.notification.service.NotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification Preferences", description = "User notification preference endpoints")
@RestController
@RequestMapping("/api/notification-preferences")
public class NotificationPreferenceController {
    private final NotificationPreferenceService notificationPreferenceService;

    public NotificationPreferenceController(NotificationPreferenceService notificationPreferenceService) {
        this.notificationPreferenceService = notificationPreferenceService;
    }

    @Operation(summary = "Get notification preferences for a user")
    @GetMapping("/{username}")
    public NotificationPreferenceResponse getPreferences(@PathVariable String username) {
        return NotificationPreferenceResponse.fromEntity(notificationPreferenceService.findOrCreate(username));
    }

    @Operation(summary = "Update notification preferences for a user")
    @PutMapping("/{username}")
    public NotificationPreferenceResponse updatePreferences(
            @PathVariable String username,
            @RequestBody NotificationPreferenceRequest request
    ) {
        return NotificationPreferenceResponse.fromEntity(notificationPreferenceService.update(
                username,
                request.emailEnabled(),
                request.pushEnabled()
        ));
    }
}
