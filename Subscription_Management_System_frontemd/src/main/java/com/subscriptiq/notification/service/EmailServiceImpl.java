package com.subscriptiq.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${application.mail.from:noreply@subscriptiq.com}")
    private String mailFrom;

    @Override
    public void sendPasswordResetEmail(String toEmail, String recipientName, String resetToken) {
        if (!StringUtils.hasText(mailHost) || mailSender == null) {
            log.info("Email service: SMTP host not configured. Simulated password reset email sent to recipient [{}].", toEmail);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(toEmail);
            message.setSubject("SubscriptIQ - Password Reset Request");
            message.setText(String.format(
                    "Hello %s,\n\n"
                            + "You requested a password reset for your SubscriptIQ account.\n\n"
                            + "Your reset token is:\n%s\n\n"
                            + "This token will expire in 15 minutes.\n"
                            + "If you did not request this, please ignore this email.\n\n"
                            + "Best regards,\nSubscriptIQ Team",
                    recipientName, resetToken
            ));

            mailSender.send(message);
            log.info("Password reset email successfully dispatched to [{}]", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to [{}]: {}", toEmail, e.getMessage());
            // Do not rethrow to avoid revealing email existence or breaking user flow
        }
    }
}

