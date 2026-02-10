package com.smartparking.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("YOUR_EMAIL@gmail.com"); // Must match application.properties
            message.setTo(toEmail);
            message.setSubject("SmartPark Login OTP");
            message.setText("Your OTP is: " + otp);

            mailSender.send(message);
            System.out.println("✅ Email Sent to " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }

    public void sendMagicLink(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("YOUR_EMAIL@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Log in to SmartPark");

            // Point this to your React Frontend URL
            String link = "http://localhost:5173/magic-login?token=" + token;

            message.setText("Click the link below to log in instantly:\n\n" + link + "\n\nThis link expires soon.");

            mailSender.send(message);
            System.out.println("✅ Magic Link sent to " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }
}