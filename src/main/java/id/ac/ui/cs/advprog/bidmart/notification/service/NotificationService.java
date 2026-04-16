package id.ac.ui.cs.advprog.bidmart.notification.service;

import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationEntity;
import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationType;

import java.util.List;

public interface NotificationService {
    NotificationEntity createNotification(String username, NotificationType type, String message, Long orderId);

    List<NotificationEntity> findByUsername(String username);

    NotificationEntity markAsRead(Long notificationId);
}