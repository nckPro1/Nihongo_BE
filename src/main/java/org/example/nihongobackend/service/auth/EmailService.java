package org.example.nihongobackend.service.auth;

public interface EmailService {
    void sendEmailVerificationLink(String toEmail, String name, String verificationLink);
    void sendResetPasswordLink(String toEmail, String name, String resetLink);
}
