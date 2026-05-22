package id.ac.ui.cs.advprog.bidmart.notification.service;

import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationPreference;

public interface NotificationPreferenceService {
    NotificationPreference findOrCreate(String username);

    NotificationPreference update(String username, boolean emailEnabled, boolean pushEnabled);
}
