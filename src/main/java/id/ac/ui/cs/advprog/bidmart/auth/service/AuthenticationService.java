package id.ac.ui.cs.advprog.bidmart.auth.service;

import id.ac.ui.cs.advprog.bidmart.auth.service.dto.AuthTokens;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.LoginCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.RefreshCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.RegisterCommand;

public interface AuthenticationService {
    void register(RegisterCommand command);

    AuthTokens login(LoginCommand command);

    AuthTokens refresh(RefreshCommand command);
}
