package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallengePurpose;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingTwoFactorCodeSenderTest {
    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private JavaMailSender mailSender;

    private LoggingTwoFactorCodeSender codeSender;
    private AuthUser user;

    @BeforeEach
    void setUp() {
        codeSender = new LoggingTwoFactorCodeSender(mailSenderProvider);
        user = new AuthUser();
        user.setEmail("buyer@example.com");
    }

    @Test
    void sendEmailOtpUsesAvailableMailSender() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        codeSender.send(user, TwoFactorMethod.EMAIL_OTP, TwoFactorChallengePurpose.LOGIN, "123456");

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();
        assertArrayEquals(new String[] {"buyer@example.com"}, message.getTo());
        assertEquals("BidMart verification code", message.getSubject());
        assertEquals("Your BidMart login verification code is: 123456", message.getText());
    }

    @Test
    void sendEmailOtpFallsBackToLogWhenMailSenderMissing() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);

        codeSender.send(user, TwoFactorMethod.EMAIL_OTP, TwoFactorChallengePurpose.ENABLE, "123456");

        verifyNoInteractions(mailSender);
    }

    @Test
    void sendEmailOtpFallsBackToLogWhenMailSenderFails() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        org.mockito.Mockito.doThrow(new MailSendException("down"))
            .when(mailSender)
            .send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));

        codeSender.send(user, TwoFactorMethod.EMAIL_OTP, TwoFactorChallengePurpose.CHANGE, "123456");

        verify(mailSender).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }

    @Test
    void sendTotpDoesNotTryMailSender() {
        codeSender.send(user, TwoFactorMethod.TOTP, TwoFactorChallengePurpose.DISABLE, null);

        verify(mailSenderProvider, never()).getIfAvailable();
    }
}
