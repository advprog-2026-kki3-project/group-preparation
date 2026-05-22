package id.ac.ui.cs.advprog.bidmart.notification.repository;

import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, String> {
}
