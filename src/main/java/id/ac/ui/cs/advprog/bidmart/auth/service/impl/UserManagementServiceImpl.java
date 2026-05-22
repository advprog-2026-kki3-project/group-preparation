package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.exception.ResourceNotFoundException;
import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserStatus;
import id.ac.ui.cs.advprog.bidmart.auth.repository.AuthUserRepository;
import id.ac.ui.cs.advprog.bidmart.auth.service.SessionService;
import id.ac.ui.cs.advprog.bidmart.auth.service.UserManagementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class UserManagementServiceImpl implements UserManagementService {
    private final AuthUserRepository authUserRepository;
    private final SessionService sessionService;
    private final Clock clock;

    public UserManagementServiceImpl(
        AuthUserRepository authUserRepository,
        SessionService sessionService,
        Clock clock
    ) {
        this.authUserRepository = authUserRepository;
        this.sessionService = sessionService;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthUser> listUsers() {
        return authUserRepository.findAllByOrderByEmailAsc();
    }

    @Override
    @Transactional
    public AuthUser disableUser(UUID userId) {
        AuthUser user = authUserRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setStatus(UserStatus.DISABLED);
        user.setDisabledAt(Instant.now(clock));
        AuthUser saved = authUserRepository.save(user);
        sessionService.revokeAllSessionsForUser(userId, "Revoked because user was disabled");
        return saved;
    }
}
