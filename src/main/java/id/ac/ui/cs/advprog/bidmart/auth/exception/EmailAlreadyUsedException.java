package id.ac.ui.cs.advprog.bidmart.auth.exception;

public class EmailAlreadyUsedException extends AuthException {
    public EmailAlreadyUsedException() {
        super("Email is already registered.");
    }
}
