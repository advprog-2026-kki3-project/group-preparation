package id.ac.ui.cs.advprog.bidmart.auth.service;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallenge;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallengePurpose;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorMethod;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserTwoFactorSettings;

import java.util.Optional;
import java.util.UUID;

public interface TwoFactorService {
    UserTwoFactorSettings getOrCreateSettings(AuthUser user);

    Optional<UserTwoFactorSettings> findSettings(UUID userId);

    TwoFactorChallenge createChallenge(AuthUser user, TwoFactorChallengePurpose purpose, TwoFactorMethod method);

    TwoFactorChallenge consumeChallenge(UUID challengeId, String code, TwoFactorChallengePurpose expectedPurpose);

    TwoFactorChallenge beginEnable(UUID userId, TwoFactorMethod method);

    UserTwoFactorSettings confirmEnable(UUID challengeId, String code);

    TwoFactorChallenge beginChange(UUID userId, TwoFactorMethod method);

    UserTwoFactorSettings confirmChange(UUID challengeId, String code);

    TwoFactorChallenge beginDisable(UUID userId);

    UserTwoFactorSettings confirmDisable(UUID challengeId, String code);
}
