package com.smartparking.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    @Autowired
    private EmailService emailService;

    // Existing OTP Storage (Email -> OTP)
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    // ✅ NEW: Magic Link Storage (Token -> Email)
    private final Map<String, String> magicLinkStorage = new ConcurrentHashMap<>();

    // --- Existing OTP Methods ---
    public void generateAndSendOtp(String email) {
        String otp = String.format("%06d", new java.util.Random().nextInt(1000000));
        otpStorage.put(email, otp);
        emailService.sendOtpEmail(email, otp);
        System.out.println("Generated OTP for " + email + ": " + otp);
    }

    public boolean validateOtp(String email, String otpInput) {
        if (!otpStorage.containsKey(email))
            return false;
        return otpStorage.get(email).equals(otpInput);
    }

    // --- ✅ NEW: Magic Link Methods ---
    public void generateAndSendMagicLink(String email) {
        // 1. Generate a long, unique token
        String token = UUID.randomUUID().toString();

        // 2. Store it (Token -> Email) so we can look up who clicked it
        magicLinkStorage.put(token, email);

        // 3. Send Email
        emailService.sendMagicLink(email, token);
        System.out.println("Generated Magic Link for " + email + ": " + token);
    }

    public String validateMagicToken(String token) {
        // Returns the email if valid, or null if invalid
        return magicLinkStorage.remove(token); // .remove() ensures the link works only once
    }
}