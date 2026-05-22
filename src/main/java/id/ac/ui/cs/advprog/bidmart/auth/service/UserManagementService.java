package id.ac.ui.cs.advprog.bidmart.auth.service;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;

import java.util.List;
import java.util.UUID;

public interface UserManagementService {
    List<AuthUser> listUsers();

    AuthUser disableUser(UUID userId);
}
