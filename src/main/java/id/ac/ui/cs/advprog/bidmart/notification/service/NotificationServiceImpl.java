package id.ac.ui.cs.advprog.bidmart.notification.service;

import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthUserRepository;
import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationEntity;
import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationPreference;
import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationType;
import id.ac.ui.cs.advprog.bidmart.notification.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceService notificationPreferenceService;
    private final AuthUserRepository authUserRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            NotificationPreferenceService notificationPreferenceService,
            AuthUserRepository authUserRepository,
            ObjectProvider<JavaMailSender> mailSenderProvider
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationPreferenceService = notificationPreferenceService;
        this.authUserRepository = authUserRepository;
        this.mailSenderProvider = mailSenderProvider;
    }

    public NotificationEntity createNotification(String username, NotificationType type, String message, Long orderId) {
        return createNotification(username, type, message, orderId, null);
    }

    @Override
    public NotificationEntity createNotification(String username, NotificationType type, String message, Long orderId, String auctionId) {
        log.info("Creating notification username={} type={} orderId={} auctionId={}", username, type, orderId, auctionId);
        NotificationEntity notification = new NotificationEntity(username, type, message, orderId, auctionId);
        NotificationPreference preference = notificationPreferenceService.findOrCreate(username);

        if (preference.isEmailEnabled()) {
            sendEmail(username, type, message);
        }

        if (!preference.isPushEnabled()) {
            log.info("Skipping in-app notification because push is disabled username={} type={}", username, type);
            return notification;
        }

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

    private void sendEmail(String username, NotificationType type, String body) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.debug("JavaMailSender is unavailable; skipping email notification username={} type={}", username, type);
            return;
        }

        try {
            UUID userId = UUID.fromString(username);
            authUserRepository.findById(userId).ifPresent(user -> {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("BidMart notification: " + type.name().replace('_', ' '));
                message.setText(body);
                mailSender.send(message);
            });
        } catch (IllegalArgumentException exception) {
            log.debug("Notification username is not a user UUID; skipping email notification username={}", username);
        } catch (RuntimeException exception) {
            log.warn("Failed to send email notification username={} type={}", username, type, exception);
        }
    }
}
