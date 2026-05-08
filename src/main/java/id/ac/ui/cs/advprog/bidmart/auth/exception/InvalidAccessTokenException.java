package id.ac.ui.cs.advprog.bidmart.auth.exception;

public class InvalidAccessTokenException extends AuthException {
    public InvalidAccessTokenException() {
        super("Access token is invalid or expired.");
    }
}
