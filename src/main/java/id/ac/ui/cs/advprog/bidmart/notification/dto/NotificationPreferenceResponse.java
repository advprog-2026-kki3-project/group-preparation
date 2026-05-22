package id.ac.ui.cs.advprog.bidmart.notification.dto;

import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationPreference;

import java.time.Instant;

public record NotificationPreferenceResponse(
        String username,
        boolean emailEnabled,
        boolean pushEnabled,
        Instant updatedAt
) {
    public static NotificationPreferenceResponse fromEntity(NotificationPreference preference) {
        return new NotificationPreferenceResponse(
                preference.getUsername(),
                preference.isEmailEnabled(),
                preference.isPushEnabled(),
                preference.getUpdatedAt()
        );
    }
}
