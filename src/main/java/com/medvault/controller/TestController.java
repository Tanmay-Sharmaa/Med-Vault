package com.medvault.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final JavaMailSender mailSender;

    @GetMapping("/test-email")
    public String sendTestEmail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("test@medvault.com"); // any dummy email
            message.setSubject("✅ Test Email from MedVault");
            message.setText("This is a test email using Mailtrap SMTP!");
            mailSender.send(message);
            return "✅ Email sent successfully via Mailtrap!";
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error sending email: " + e.getMessage();
        }
    }
}

