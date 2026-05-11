package id.ac.ui.cs.advprog.bidmart.auth.service;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;

import java.util.UUID;

public interface UserManagementService {
    AuthUser disableUser(UUID userId);
}
