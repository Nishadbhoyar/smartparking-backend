package com.smartparking.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // This will use localhost for your PC, but use the Vercel URL on Render
    @Value("${app.frontend.url:http://localhost:5173}")
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

    public void sendMagicLink(String toEmail, String token) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        String link = frontendUrl + "/magic-login?token=" + token;
        String htmlMsg = "<h3>Welcome to ParkEase</h3>" +
                "<p>Click the button below to log in:</p>" +
                "<a href='" + link
                + "' style='background: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Log In Now</a>";

        helper.setText(htmlMsg, true); // The 'true' flag means it's HTML
        helper.setTo(toEmail);
        helper.setSubject("Log in to SmartPark");
        helper.setFrom(SENDER_EMAIL);

        mailSender.send(mimeMessage);
    }
}