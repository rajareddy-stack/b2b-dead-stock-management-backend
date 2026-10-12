package com.deadstock.marketplace.service;

import com.deadstock.marketplace.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean enabled;
    private final String mailHost;
    private final String fromAddress;
    private final String publicUrl;

    public EmailNotificationService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.notifications.email.enabled:false}") boolean enabled,
            @Value("${spring.mail.host:}") String mailHost,
            @Value("${app.notifications.email.from:no-reply@deadstockmarketplace.com}") String fromAddress,
            @Value("${app.public-url:https://b2b-dead-stock-management-frontend.vercel.app}") String publicUrl) {
        this.mailSenderProvider = mailSenderProvider;
        this.enabled = enabled;
        this.mailHost = mailHost;
        this.fromAddress = fromAddress;
        this.publicUrl = publicUrl;
    }

    public void send(User recipient, String title, String message, String linkUrl) {
        if (!enabled) {
            return;
        }
        if (mailHost.isBlank()) {
            logger.error("Email notification is enabled but SMTP_HOST is not configured");
            return;
        }
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            logger.error("Email notification is enabled but no JavaMailSender is configured");
            return;
        }

        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(fromAddress);
        email.setTo(recipient.getEmail());
        email.setSubject(title);
        String link = linkUrl == null || linkUrl.isBlank() ? "" : "\n\nOpen your account: " + publicUrl + linkUrl;
        email.setText(message + link);
        try {
            mailSender.send(email);
        } catch (MailException exception) {
            logger.error("Could not send notification email to {}", recipient.getEmail(), exception);
        }
    }
}
