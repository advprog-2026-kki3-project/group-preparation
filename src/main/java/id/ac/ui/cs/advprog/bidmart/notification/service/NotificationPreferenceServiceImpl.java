package id.ac.ui.cs.advprog.bidmart.notification.service;

import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationPreference;
import id.ac.ui.cs.advprog.bidmart.notification.repository.NotificationPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Service
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {
    private final NotificationPreferenceRepository notificationPreferenceRepository;

    public NotificationPreferenceServiceImpl(NotificationPreferenceRepository notificationPreferenceRepository) {
        this.notificationPreferenceRepository = notificationPreferenceRepository;
    }

    @Override
    @Transactional
    public NotificationPreference findOrCreate(String username) {
        validateUsername(username);
        return notificationPreferenceRepository.findById(username)
                .orElseGet(() -> notificationPreferenceRepository.save(new NotificationPreference(username)));
    }

    @Override
    @Transactional
    public NotificationPreference update(String username, boolean emailEnabled, boolean pushEnabled) {
        NotificationPreference preference = findOrCreate(username);
        preference.update(emailEnabled, pushEnabled, Instant.now());
        return notificationPreferenceRepository.save(preference);
    }

    private void validateUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Username must be provided");
        }
    }
}
