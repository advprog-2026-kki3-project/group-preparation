package id.ac.ui.cs.advprog.bidmart.auth.exception;

public class InvalidTwoFactorCodeException extends AuthException {
    public InvalidTwoFactorCodeException() {
        super("Invalid or expired two-factor authentication code.");
    }
}
