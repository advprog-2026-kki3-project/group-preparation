package id.ac.ui.cs.advprog.bidmart.auth.exception;

public class OtpAttemptLimitExceededException extends AuthException {
    public OtpAttemptLimitExceededException(long retryAfterSeconds) {
        super("Maximum OTP attempts exceeded. Try again in " + formatDuration(retryAfterSeconds) + ".");
    }

    private static String formatDuration(long seconds) {
        long safeSeconds = Math.max(1, seconds);
        long minutes = safeSeconds / 60;
        long remainingSeconds = safeSeconds % 60;
        if (minutes == 0) {
            return remainingSeconds + " second" + (remainingSeconds == 1 ? "" : "s");
        }
        if (remainingSeconds == 0) {
            return minutes + " minute" + (minutes == 1 ? "" : "s");
        }
        return minutes + " minute" + (minutes == 1 ? "" : "s")
            + " " + remainingSeconds + " second" + (remainingSeconds == 1 ? "" : "s");
    }
}
