package id.ac.ui.cs.advprog.bidmart.auth.controller;

import id.ac.ui.cs.advprog.bidmart.auth.controller.dto.ErrorResponse;
import id.ac.ui.cs.advprog.bidmart.auth.exception.TwoFactorRequiredException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.Instant;

@RestControllerAdvice
public class SecurityExceptionHandler {
    private final Clock clock;

    public SecurityExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(TwoFactorRequiredException.class)
    public ResponseEntity<ErrorResponse> handleTwoFactorRequired(
        TwoFactorRequiredException exception,
        HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(
            Instant.now(clock),
            HttpStatus.FORBIDDEN.value(),
            HttpStatus.FORBIDDEN.getReasonPhrase(),
            exception.getMessage(),
            request.getRequestURI()
        ));
    }
}
