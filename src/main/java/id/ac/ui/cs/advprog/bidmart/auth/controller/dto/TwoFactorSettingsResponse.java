package id.ac.ui.cs.advprog.bidmart.auth.controller.dto;

public record TwoFactorSettingsResponse(
    boolean enabled,
    String method,
    String pendingMethod
) {
}
