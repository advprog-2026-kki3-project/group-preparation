package id.ac.ui.cs.advprog.bidmart.auth.service.impl;

import id.ac.ui.cs.advprog.bidmart.auth.model.AuthUser;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorChallengePurpose;
import id.ac.ui.cs.advprog.bidmart.auth.model.TwoFactorMethod;
import id.ac.ui.cs.advprog.bidmart.auth.service.TwoFactorCodeSender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingTwoFactorCodeSender implements TwoFactorCodeSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingTwoFactorCodeSender.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public LoggingTwoFactorCodeSender(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    @Override
    public void send(AuthUser user, TwoFactorMethod method, TwoFactorChallengePurpose purpose, String code) {
        if (method == TwoFactorMethod.EMAIL_OTP && trySendEmail(user, purpose, code)) {
            return;
        }
        LOGGER.info("2FA {} code for {} using {}: {}", purpose, user.getEmail(), method, code);
    }

    private boolean trySendEmail(AuthUser user, TwoFactorChallengePurpose purpose, String code) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("BidMart verification code");
            message.setText("Your BidMart " + purpose.name().toLowerCase() + " verification code is: " + code);
            mailSender.send(message);
            return true;
        } catch (MailException exception) {
            LOGGER.warn("Could not send 2FA email to {}; falling back to log output.", user.getEmail(), exception);
            return false;
        }
    }
}
