package id.ac.ui.cs.advprog.bidmart.auth.exception;

public class InvalidRefreshTokenException extends AuthException {
    public InvalidRefreshTokenException() {
        super("Refresh token is invalid or expired.");
    }
}
