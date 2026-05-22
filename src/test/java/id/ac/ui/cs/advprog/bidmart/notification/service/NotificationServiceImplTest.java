package id.ac.ui.cs.advprog.bidmart.notification.service;

import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthUserRepository;
import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationEntity;
import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationPreference;
import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationType;
import id.ac.ui.cs.advprog.bidmart.notification.repository.NotificationRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationPreferenceService notificationPreferenceService;

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void createNotification_savesEntity() {
        when(notificationPreferenceService.findOrCreate("buyer")).thenReturn(new NotificationPreference("buyer"));
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);
        NotificationEntity expected = new NotificationEntity("buyer", NotificationType.ORDER_CREATED, "created", 1L);
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(expected);

        NotificationEntity result = notificationService.createNotification("buyer", NotificationType.ORDER_CREATED, "created", 1L);

        assertThat(result.getUsername()).isEqualTo("buyer");
        assertThat(result.getType()).isEqualTo(NotificationType.ORDER_CREATED);
    }

    @Test
    void createNotification_skipsSavingWhenPushDisabled() {
        NotificationPreference preference = new NotificationPreference("buyer");
        preference.update(true, false, java.time.Instant.now());
        when(notificationPreferenceService.findOrCreate("buyer")).thenReturn(preference);
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);

        NotificationEntity result = notificationService.createNotification("buyer", NotificationType.ORDER_CREATED, "created", 1L);

        assertThat(result.getUsername()).isEqualTo("buyer");
        verify(notificationRepository, never()).save(any(NotificationEntity.class));
    }

    @Test
    void markAsRead_updatesReadFlag() {
        NotificationEntity entity = new NotificationEntity("buyer", NotificationType.ORDER_CREATED, "created", 1L);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(notificationRepository.save(any(NotificationEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationEntity result = notificationService.markAsRead(1L);

        assertThat(result.isRead()).isTrue();
    }
}
