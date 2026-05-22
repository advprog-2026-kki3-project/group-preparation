package id.ac.ui.cs.advprog.bidmart.notification.dto;

public record NotificationPreferenceRequest(
        boolean emailEnabled,
        boolean pushEnabled
) {
}
