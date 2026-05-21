package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.config.AuthProperties;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthPolicySettings;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthPolicySettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthPolicyServiceImplTest {
    @Mock
    private AuthPolicySettingsRepository policyRepository;

    private AuthPolicyServiceImpl policyService;

    @BeforeEach
    void setUp() {
        AuthProperties properties = new AuthProperties();
        properties.setMaxConcurrentSessions(3);
        properties.setConcurrentSessionPolicy(AuthProperties.ConcurrentSessionPolicy.REVOKE_OLDEST);
        properties.setLoginAttemptLimit(5);
        properties.setLoginAttemptWindow(Duration.ofMinutes(15));
        properties.setOtpAttemptLimit(5);
        properties.setOtpTtl(Duration.ofMinutes(5));
        policyService = new AuthPolicyServiceImpl(policyRepository, properties);
    }

    @Test
    void getPolicyCreatesDefaultPolicyWhenMissing() {
        when(policyRepository.findById(AuthPolicySettings.SINGLETON_ID)).thenReturn(Optional.empty());
        when(policyRepository.save(any(AuthPolicySettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthPolicySettings policy = policyService.getPolicy();

        assertEquals(3, policy.getMaxConcurrentSessions());
        assertEquals(AuthProperties.ConcurrentSessionPolicy.REVOKE_OLDEST, policy.getConcurrentSessionPolicy());
        assertEquals(5, policy.getLoginAttemptLimit());
        assertEquals(Duration.ofMinutes(15), policy.getLoginAttemptWindow());
    }

    @Test
    void updatePolicyRejectsInvalidLimits() {
        assertThrows(IllegalArgumentException.class, () -> policyService.updatePolicy(
            0,
            AuthProperties.ConcurrentSessionPolicy.REJECT_NEW,
            5,
            Duration.ofMinutes(15),
            5,
            Duration.ofMinutes(5)
        ));
    }

    @Test
    void updatePolicyRejectsNullConcurrentSessionPolicy() {
        assertThrows(IllegalArgumentException.class, () -> policyService.updatePolicy(
            3,
            null,
            5,
            Duration.ofMinutes(15),
            5,
            Duration.ofMinutes(5)
        ));
    }

    @Test
    void updatePolicyRejectsInvalidDurations() {
        assertThrows(IllegalArgumentException.class, () -> policyService.updatePolicy(
            3,
            AuthProperties.ConcurrentSessionPolicy.REJECT_NEW,
            5,
            null,
            5,
            Duration.ofMinutes(5)
        ));
        assertThrows(IllegalArgumentException.class, () -> policyService.updatePolicy(
            3,
            AuthProperties.ConcurrentSessionPolicy.REJECT_NEW,
            5,
            Duration.ZERO,
            5,
            Duration.ofMinutes(5)
        ));
        assertThrows(IllegalArgumentException.class, () -> policyService.updatePolicy(
            3,
            AuthProperties.ConcurrentSessionPolicy.REJECT_NEW,
            5,
            Duration.ofMinutes(15),
            5,
            Duration.ofSeconds(-1)
        ));
    }
}
