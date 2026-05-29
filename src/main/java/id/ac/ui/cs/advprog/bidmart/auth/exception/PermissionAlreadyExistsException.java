package id.ac.ui.cs.advprog.bidmart.auth.exception;

public class PermissionAlreadyExistsException extends AuthException {
    public PermissionAlreadyExistsException() {
        super("Permission already exists.");
    }
}
