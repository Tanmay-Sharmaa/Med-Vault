package com.medvault.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Sends a verification email to newly registered users.
     *
     * @param to Recipient email address
     * @param messageBody Verification message (usually includes a link)
     */
    public void sendVerificationEmail(String to, String messageBody) {
        String subject = "Verify your MedVault Account";

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(messageBody);

        mailSender.send(mail);
    }

    /**
     * Sends any custom email (for generic use like OTP, alerts, etc.)
     *
     * @param to Recipient email address
     * @param subject Email subject line
     * @param messageBody Email body content
     */
    public void sendCustomEmail(String to, String subject, String messageBody) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(messageBody);

        mailSender.send(mail);
    }

    /**
     * 🆕 Sends password reset link email to the user.
     *
     * @param to Recipient email address
     * @param token Unique reset token
     */
    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Reset Your MedVault Password";

        String resetUrl = "http://localhost:8080/reset-password?token=" + token;

        String messageBody = """
                Dear User,

                We received a request to reset your MedVault password.
                Click the link below to set a new password:

                """ + resetUrl + """

                This link will expire in 15 minutes.
                If you did not request this, please ignore this email.

                — MedVault Team
                """;

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(messageBody);

        mailSender.send(mail);
    }
}
