package id.ac.ui.cs.advprog.bidmart.notification.service;

import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationPreference;
import id.ac.ui.cs.advprog.bidmart.notification.repository.NotificationPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceServiceImplTest {
    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;

    private NotificationPreferenceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationPreferenceServiceImpl(notificationPreferenceRepository);
    }

    @Test
    void findOrCreate_returnsExistingPreference() {
        NotificationPreference preference = new NotificationPreference("alice");
        when(notificationPreferenceRepository.findById("alice")).thenReturn(Optional.of(preference));

        NotificationPreference result = service.findOrCreate("alice");

        assertThat(result).isSameAs(preference);
    }

    @Test
    void findOrCreate_createsDefaultPreference() {
        when(notificationPreferenceRepository.findById("alice")).thenReturn(Optional.empty());
        when(notificationPreferenceRepository.save(any(NotificationPreference.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationPreference result = service.findOrCreate("alice");

        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.isEmailEnabled()).isTrue();
        assertThat(result.isPushEnabled()).isTrue();
        verify(notificationPreferenceRepository).save(any(NotificationPreference.class));
    }

    @Test
    void update_persistsPreferenceFlags() {
        NotificationPreference preference = new NotificationPreference("alice");
        when(notificationPreferenceRepository.findById("alice")).thenReturn(Optional.of(preference));
        when(notificationPreferenceRepository.save(any(NotificationPreference.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationPreference result = service.update("alice", false, true);

        assertThat(result.isEmailEnabled()).isFalse();
        assertThat(result.isPushEnabled()).isTrue();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void findOrCreate_rejectsBlankUsername() {
        assertThatThrownBy(() -> service.findOrCreate(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username must be provided");
    }
}
