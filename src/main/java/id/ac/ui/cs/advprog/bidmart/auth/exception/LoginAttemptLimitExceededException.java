package id.ac.ui.cs.advprog.bidmart.auth.exception;

public class LoginAttemptLimitExceededException extends AuthException {
    public LoginAttemptLimitExceededException() {
        super("Maximum login attempts exceeded.");
    }
}
