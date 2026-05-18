package id.ac.ui.cs.advprog.bidmart.auth.controller;

import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.BeginTwoFactorRequest;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.TwoFactorChallengeResponse;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.TwoFactorSettingsResponse;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.TwoFactorVerifyRequest;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallenge;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorMethod;
import id.ac.ui.cs.advprog.bidmart.auth.model.UserTwoFactorSettings;
import id.ac.ui.cs.advprog.bidmart.auth.security.RequiresPermission;
import id.ac.ui.cs.advprog.bidmart.auth.service.TotpService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TwoFactorService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth/2fa")
@RequiresPermission
public class AccountSecurityController {
    private final TwoFactorService twoFactorService;
    private final TotpService totpService;

    public AccountSecurityController(TwoFactorService twoFactorService, TotpService totpService) {
        this.twoFactorService = twoFactorService;
        this.totpService = totpService;
    }

    @GetMapping
    public TwoFactorSettingsResponse getSettings(Authentication authentication) {
        return twoFactorService.findSettings(currentUserId(authentication))
            .map(this::toSettingsResponse)
            .orElse(new TwoFactorSettingsResponse(false, null, null));
    }

    @PostMapping("/enable")
    public TwoFactorChallengeResponse beginEnable(
        @Valid @RequestBody BeginTwoFactorRequest request,
        Authentication authentication
    ) {
        TwoFactorChallenge challenge = twoFactorService.beginEnable(currentUserId(authentication), request.method());
        return toChallengeResponse(challenge);
    }

    @PostMapping("/change")
    public TwoFactorChallengeResponse beginChange(
        @Valid @RequestBody BeginTwoFactorRequest request,
        Authentication authentication
    ) {
        TwoFactorChallenge challenge = twoFactorService.beginChange(currentUserId(authentication), request.method());
        return toChallengeResponse(challenge);
    }

    @PostMapping("/enable/confirm")
    public TwoFactorSettingsResponse confirmEnable(@Valid @RequestBody TwoFactorVerifyRequest request) {
        return toSettingsResponse(twoFactorService.confirmEnable(request.challengeId(), request.code()));
    }

    @PostMapping("/change/confirm")
    public TwoFactorSettingsResponse confirmChange(@Valid @RequestBody TwoFactorVerifyRequest request) {
        return toSettingsResponse(twoFactorService.confirmChange(request.challengeId(), request.code()));
    }

    @PostMapping("/disable")
    public TwoFactorChallengeResponse beginDisable(Authentication authentication) {
        TwoFactorChallenge challenge = twoFactorService.beginDisable(currentUserId(authentication));
        return toChallengeResponse(challenge);
    }

    @PostMapping("/disable/confirm")
    public TwoFactorSettingsResponse confirmDisable(@Valid @RequestBody TwoFactorVerifyRequest request) {
        return toSettingsResponse(twoFactorService.confirmDisable(request.challengeId(), request.code()));
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private TwoFactorSettingsResponse toSettingsResponse(UserTwoFactorSettings settings) {
        return new TwoFactorSettingsResponse(
            settings.isEnabled(),
            settings.getMethod() == null ? null : settings.getMethod().name(),
            settings.getPendingMethod() == null ? null : settings.getPendingMethod().name()
        );
    }

    private TwoFactorChallengeResponse toChallengeResponse(TwoFactorChallenge challenge) {
        String secret = null;
        String uri = null;
        if (challenge.getMethod() == TwoFactorMethod.TOTP) {
            secret = twoFactorService.findSettings(challenge.getUser().getId())
                .map(UserTwoFactorSettings::getPendingTotpSecret)
                .orElse(null);
            if (secret != null) {
                uri = totpService.buildOtpAuthUri("BidMart", challenge.getUser().getEmail(), secret);
            }
        }
        return new TwoFactorChallengeResponse(
            challenge.getId(),
            challenge.getMethod().name(),
            challenge.getExpiresAt(),
            secret,
            uri
        );
    }
}
