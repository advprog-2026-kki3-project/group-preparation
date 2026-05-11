package id.ac.ui.cs.advprog.bidmart.auth.exception;

public class ForbiddenPermissionException extends AuthException {
    public ForbiddenPermissionException() {
        super("The authenticated user is not allowed to perform this operation.");
    }
}
