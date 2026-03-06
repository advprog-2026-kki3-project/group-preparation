package id.ac.ui.cs.advprog.bidmart.auth.exception;

public class UserDisabledException extends AuthException {
    public UserDisabledException() {
        super("User account is disabled.");
    }
}
