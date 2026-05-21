package id.ac.ui.cs.advprog.bidmart.auth.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttemptLimitExceededExceptionTest {

    @Test
    void loginLimitMessageUsesSingularSecond() {
        LoginAttemptLimitExceededException exception = new LoginAttemptLimitExceededException(1);

        assertEquals("Maximum login attempts exceeded. Try again in 1 second.", exception.getMessage());
    }

    @Test
    void loginLimitMessageUsesPluralSeconds() {
        LoginAttemptLimitExceededException exception = new LoginAttemptLimitExceededException(2);

        assertEquals("Maximum login attempts exceeded. Try again in 2 seconds.", exception.getMessage());
    }

    @Test
    void loginLimitMessageUsesSingularMinute() {
        LoginAttemptLimitExceededException exception = new LoginAttemptLimitExceededException(60);

        assertEquals("Maximum login attempts exceeded. Try again in 1 minute.", exception.getMessage());
    }

    @Test
    void loginLimitMessageUsesPluralMinutesAndSeconds() {
        LoginAttemptLimitExceededException exception = new LoginAttemptLimitExceededException(121);

        assertEquals("Maximum login attempts exceeded. Try again in 2 minutes 1 second.", exception.getMessage());
    }

    @Test
    void otpLimitMessageClampsNonPositiveDuration() {
        OtpAttemptLimitExceededException exception = new OtpAttemptLimitExceededException(0);

        assertEquals("Maximum OTP attempts exceeded. Try again in 1 second.", exception.getMessage());
    }

    @Test
    void otpLimitMessageUsesPluralSeconds() {
        OtpAttemptLimitExceededException exception = new OtpAttemptLimitExceededException(30);

        assertEquals("Maximum OTP attempts exceeded. Try again in 30 seconds.", exception.getMessage());
    }

    @Test
    void otpLimitMessageUsesPluralMinutes() {
        OtpAttemptLimitExceededException exception = new OtpAttemptLimitExceededException(120);

        assertEquals("Maximum OTP attempts exceeded. Try again in 2 minutes.", exception.getMessage());
    }

    @Test
    void otpLimitMessageUsesSingularMinuteAndPluralSeconds() {
        OtpAttemptLimitExceededException exception = new OtpAttemptLimitExceededException(62);

        assertEquals("Maximum OTP attempts exceeded. Try again in 1 minute 2 seconds.", exception.getMessage());
    }
}
