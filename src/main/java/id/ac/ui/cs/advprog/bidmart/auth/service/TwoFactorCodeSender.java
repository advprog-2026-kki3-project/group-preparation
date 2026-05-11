package id.ac.ui.cs.advprog.bidmart.auth.service;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallengePurpose;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorMethod;

public interface TwoFactorCodeSender {
    void send(AuthUser user, TwoFactorMethod method, TwoFactorChallengePurpose purpose, String code);
}
