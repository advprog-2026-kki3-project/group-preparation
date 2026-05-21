package id.ac.ui.cs.advprog.bidmart.notification.service;

import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationEntity;
import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationType;
import id.ac.ui.cs.advprog.bidmart.notification.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public NotificationEntity createNotification(String username, NotificationType type, String message, Long orderId) {
        log.info("Creating notification username={} type={} orderId={}", username, type, orderId);
        NotificationEntity notification = new NotificationEntity(username, type, message, orderId);
        return notificationRepository.save(notification);
    }

    @Override
    public List<NotificationEntity> findByUsername(String username) {
        log.debug("Fetching notifications for username={}", username);
        return notificationRepository.findByUsernameOrderByCreatedAtDesc(username);
    }

    @Override
    public NotificationEntity markAsRead(Long notificationId) {
        log.info("Marking notification id={} as read", notificationId);
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found id=" + notificationId));
        notification.markRead();
        return notificationRepository.save(notification);
    }
}