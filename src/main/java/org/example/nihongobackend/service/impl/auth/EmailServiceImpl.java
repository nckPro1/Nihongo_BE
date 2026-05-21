package org.example.nihongobackend.service.impl.auth;

import org.example.nihongobackend.service.auth.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmailVerificationLink(String toEmail, String name, String verificationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Zenigo - Verify your email");
        message.setText(
                "Hi " + name + ",\n\n"
                        + "Please verify your email by clicking the link below:\n"
                        + verificationLink + "\n\n"
                        + "This link will expire in 30 minutes.\n\n"
                        + "If you did not register, please ignore this email."
        );
        mailSender.send(message);
    }

    @Override
    public void sendResetPasswordLink(String toEmail, String name, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Zenigo - Reset your password");
        message.setText(
                "Hi " + name + ",\n\n"
                        + "Please reset your password by clicking the link below:\n"
                        + resetLink + "\n\n"
                        + "This link will expire in 30 minutes.\n\n"
                        + "If you did not request password reset, please ignore this email."
        );
        mailSender.send(message);
    }
}
