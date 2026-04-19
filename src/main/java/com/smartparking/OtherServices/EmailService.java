package com.smartparking.OtherServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends a 6-digit OTP to the user's email for password reset.
     */
    public void sendPasswordResetOtp(String toEmail, String userName, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("SmartParking — Password Reset OTP");
        message.setText(
                "Hi " + userName + ",\n\n" +
                        "You requested a password reset for your SmartParking account.\n\n" +
                        "Your OTP is: " + otp + "\n\n" +
                        "This OTP is valid for 15 minutes. Do not share it with anyone.\n\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "— The SmartParking Team"
        );
        mailSender.send(message);
    }
}