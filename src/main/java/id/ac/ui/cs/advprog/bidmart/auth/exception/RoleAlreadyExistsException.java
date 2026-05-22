package id.ac.ui.cs.advprog.bidmart.auth.exception;

public class RoleAlreadyExistsException extends AuthException {
    public RoleAlreadyExistsException() {
        super("Role already exists.");
    }
}
