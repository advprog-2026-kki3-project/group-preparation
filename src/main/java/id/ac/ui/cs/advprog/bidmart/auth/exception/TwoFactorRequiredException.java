package id.ac.ui.cs.advprog.bidmart.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TwoFactorRequiredException extends RuntimeException {
    public TwoFactorRequiredException() {
        super("2FA_REQUIRED");
    }
}