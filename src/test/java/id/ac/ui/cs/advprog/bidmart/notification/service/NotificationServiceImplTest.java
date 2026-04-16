package id.ac.ui.cs.advprog.bidmart.notification.service;

import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationEntity;
import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationType;
import id.ac.ui.cs.advprog.bidmart.notification.repo.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void createNotification_savesEntity() {
        NotificationEntity expected = new NotificationEntity("buyer", NotificationType.ORDER_CREATED, "created", 1L);
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(expected);

        NotificationEntity result = notificationService.createNotification("buyer", NotificationType.ORDER_CREATED, "created", 1L);

        assertThat(result.getUsername()).isEqualTo("buyer");
        assertThat(result.getType()).isEqualTo(NotificationType.ORDER_CREATED);
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