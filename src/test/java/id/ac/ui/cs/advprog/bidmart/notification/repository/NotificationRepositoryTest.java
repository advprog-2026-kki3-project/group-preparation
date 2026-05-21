package id.ac.ui.cs.advprog.bidmart.notification.repository;

import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationEntity;
import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void findByUsernameOrderByCreatedAtDesc_returnsOnlyForUserSortedByCreatedAtDesc() throws Exception {
        NotificationEntity older = new NotificationEntity("alice", NotificationType.ORDER_CREATED, "first", 1L);
        notificationRepository.saveAndFlush(older);
        Thread.sleep(10);
        NotificationEntity newer = new NotificationEntity("alice", NotificationType.ORDER_SHIPPED, "second", 1L);
        notificationRepository.saveAndFlush(newer);
        notificationRepository.saveAndFlush(new NotificationEntity("bob", NotificationType.ORDER_CREATED, "bob", 2L));

        List<NotificationEntity> aliceNotifications = notificationRepository.findByUsernameOrderByCreatedAtDesc("alice");

        assertThat(aliceNotifications).hasSize(2);
        assertThat(aliceNotifications.get(0).getMessage()).isEqualTo("second");
        assertThat(aliceNotifications.get(1).getMessage()).isEqualTo("first");
    }

    @Test
    void findByUsernameOrderByCreatedAtDesc_returnsEmptyWhenUserHasNoNotifications() {
        List<NotificationEntity> result = notificationRepository.findByUsernameOrderByCreatedAtDesc("nobody");

        assertThat(result).isEmpty();
    }

    @Test
    void save_persistsReadFlagUpdate() {
        NotificationEntity saved = notificationRepository.save(
                new NotificationEntity("carol", NotificationType.ORDER_COMPLETED, "done", 9L));
        saved.markRead();
        notificationRepository.saveAndFlush(saved);

        NotificationEntity reloaded = notificationRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.isRead()).isTrue();
    }
}