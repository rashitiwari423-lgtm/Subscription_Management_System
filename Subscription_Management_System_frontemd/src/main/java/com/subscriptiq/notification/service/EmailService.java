package com.subscriptiq.notification.service;

public interface EmailService {

    /**
     * Sends password reset email with secure token.
     *
     * @param toEmail       recipient email
     * @param recipientName recipient full name
     * @param resetToken    secure one-time reset token
     */
    void sendPasswordResetEmail(String toEmail, String recipientName, String resetToken);
}

