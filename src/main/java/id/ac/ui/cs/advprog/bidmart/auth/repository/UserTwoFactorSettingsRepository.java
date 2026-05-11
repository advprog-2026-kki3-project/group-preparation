package id.ac.ui.cs.advprog.bidmart.auth.repository;

import id.ac.ui.cs.advprog.bidmart.auth.model.UserTwoFactorSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserTwoFactorSettingsRepository extends JpaRepository<UserTwoFactorSettings, UUID> {
}
