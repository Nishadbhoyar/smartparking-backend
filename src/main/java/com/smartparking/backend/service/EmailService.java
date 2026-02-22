package com.smartparking.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // This will use localhost for your PC, but use the Vercel URL on Render
    @Value("${app.frontend.url:https://smartparking-frontend-lilac.vercel.app}")
    private String frontendUrl;

    private static final String SENDER_EMAIL = "nishadbhoyar223@gmail.com";

    public void sendOtpEmail(String toEmail, String otp) {
        // REMOVED try-catch: If this fails, the error will now properly 
        // reach your Controller so it can send a 500 error to the frontend.
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(SENDER_EMAIL);
        message.setTo(toEmail);
        message.setSubject("SmartPark Login OTP");
        message.setText("Your verification code for ParkEase is: " + otp + "\n\nThis code will expire in 5 minutes.");

        mailSender.send(message);
        System.out.println("✅ Email Sent successfully to: " + toEmail);
    }

    public void sendMagicLink(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(SENDER_EMAIL);
        message.setTo(toEmail);
        message.setSubject("Log in to SmartPark");

        // ✅ Updated to use the dynamic frontendUrl variable
        String link = frontendUrl + "/magic-login?token=" + token;

        message.setText("Click the link below to log in instantly to ParkEase:\n\n" 
                        + link + "\n\nThis link expires soon.");

        mailSender.send(message);
        System.out.println("✅ Magic Link sent successfully to: " + toEmail);
    }
}