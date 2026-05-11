package id.ac.ui.cs.advprog.bidmart.auth.exception;

public class InvalidCredentialsException extends AuthException {
    public InvalidCredentialsException() {
        super("Invalid email or password.");
    }
}
