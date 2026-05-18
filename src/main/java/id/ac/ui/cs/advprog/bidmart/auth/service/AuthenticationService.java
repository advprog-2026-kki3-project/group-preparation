package id.ac.ui.cs.advprog.bidmart.auth.service;

import id.ac.ui.cs.advprog.bidmart.auth.service.dto.AuthTokens;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.LoginCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.LoginResult;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.RefreshCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.RegisterCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.TwoFactorVerifyCommand;

public interface AuthenticationService {
    void register(RegisterCommand command);

    LoginResult login(LoginCommand command);

    AuthTokens verifyTwoFactorLogin(TwoFactorVerifyCommand command);

    AuthTokens refresh(RefreshCommand command);
}
