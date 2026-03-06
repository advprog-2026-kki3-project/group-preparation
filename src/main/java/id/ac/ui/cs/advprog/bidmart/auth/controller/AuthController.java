package id.ac.ui.cs.advprog.bidmart.auth.controller;

import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.AuthTokenResponse;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.LoginRequest;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.RefreshRequest;
import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.RegisterRequest;
import id.ac.ui.cs.advprog.bidmart.auth.service.AuthenticationService;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.AuthTokens;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.LoginCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.RefreshCommand;
import id.ac.ui.cs.advprog.bidmart.auth.service.dto.RegisterCommand;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authenticationService.register(new RegisterCommand(
            request.email(),
            request.password(),
            request.role()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpServletRequest
    ) {
        AuthTokens tokens = authenticationService.login(new LoginCommand(
            request.email(),
            request.password(),
            httpServletRequest.getRemoteAddr(),
            httpServletRequest.getHeader("User-Agent")
        ));
        return ResponseEntity.ok(toResponse(tokens));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthTokens tokens = authenticationService.refresh(new RefreshCommand(request.refreshToken()));
        return ResponseEntity.ok(toResponse(tokens));
    }

    private AuthTokenResponse toResponse(AuthTokens tokens) {
        return new AuthTokenResponse(
            "Bearer",
            tokens.accessToken(),
            tokens.refreshToken(),
            tokens.accessExpiresAt(),
            tokens.refreshExpiresAt()
        );
    }
}
