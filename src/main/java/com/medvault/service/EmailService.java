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
     * Sends a verification email (or any message) to the user.
     *
     * @param to Recipient email address
     * @param messageBody Body of the email (content you want to send)
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
     * Reusable method for sending generic emails like OTP or password reset
     */
    public void sendCustomEmail(String to, String subject, String messageBody) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(messageBody);

        mailSender.send(mail);
    }
}

