package id.ac.ui.cs.advprog.bidmart.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class ProtectedAuthController {
    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        return Map.of(
            "authenticated", true,
            "principal", authentication.getName(),
            "authorities", authentication.getAuthorities().stream().map(Object::toString).toList()
        );
    }
}
