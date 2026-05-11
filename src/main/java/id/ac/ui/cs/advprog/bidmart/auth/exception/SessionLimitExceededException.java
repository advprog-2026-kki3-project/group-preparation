package id.ac.ui.cs.advprog.bidmart.auth.exception;

public class SessionLimitExceededException extends AuthException {
    public SessionLimitExceededException() {
        super("Maximum concurrent sessions exceeded.");
    }
}
