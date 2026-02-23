package com.smartparking.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.mail.MessagingException;

@Service
public class OtpService {

    @Autowired
    private EmailService emailService;

    // ✅ SecureRandom is cryptographically stronger than Random
    private final SecureRandom secureRandom = new SecureRandom();

    // ─── OTP Storage (all in-memory, zero DB) ───────────────────────────────────
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>(); // email -> otp
    private final Map<String, Long> otpExpiry = new ConcurrentHashMap<>(); // email -> expiry time
    private final Map<String, Integer> otpRequestCount = new ConcurrentHashMap<>(); // email -> request count
    private final Map<String, Long> otpRateLimitStart = new ConcurrentHashMap<>(); // email -> window start time

    // ─── Magic Link Storage (all in-memory, zero DB) ────────────────────────────
    private final Map<String, String> magicLinkStorage = new ConcurrentHashMap<>(); // token -> email
    private final Map<String, Long> magicLinkExpiry = new ConcurrentHashMap<>(); // token -> expiry time

    // ─── Constants ───────────────────────────────────────────────────────────────
    private static final long OTP_EXPIRY_MS = 5 * 60 * 1000; // 5 minutes
    private static final long MAGIC_LINK_EXPIRY_MS = 15 * 60 * 1000; // 15 minutes
    private static final long RATE_LIMIT_WINDOW_MS = 60 * 60 * 1000; // 1 hour
    private static final int MAX_OTP_PER_HOUR = 5; // max 5 OTPs per hour per email

    // ════════════════════════════════════════════════════════════════════════════
    // OTP Methods
    // ════════════════════════════════════════════════════════════════════════════

    public void generateAndSendOtp(String email) {

        // ── Rate Limit Check ──────────────────────────────────────────────────
        long now = System.currentTimeMillis();
        long windowStart = otpRateLimitStart.getOrDefault(email, 0L);
        int count = otpRequestCount.getOrDefault(email, 0);

        if (now - windowStart < RATE_LIMIT_WINDOW_MS) {
            // Still in the same 1-hour window
            if (count >= MAX_OTP_PER_HOUR) {
                throw new RuntimeException(
                        "Too many OTP requests. Please wait before requesting a new one.");
            }
            otpRequestCount.put(email, count + 1);
        } else {
            // New window — reset counter
            otpRateLimitStart.put(email, now);
            otpRequestCount.put(email, 1);
        }

        // ── Generate & Store OTP ──────────────────────────────────────────────
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        otpStorage.put(email, otp);
        otpExpiry.put(email, now + OTP_EXPIRY_MS);

        // ── Send Email ────────────────────────────────────────────────────────
        emailService.sendOtpEmail(email, otp);
        System.out.println("✅ OTP sent to: " + email + " | Expires in 5 minutes.");
    }

    public boolean validateOtp(String email, String otpInput) {
        String storedOtp = otpStorage.get(email);
        Long expiry = otpExpiry.get(email);
        long now = System.currentTimeMillis();

        // ── Always clean up after validation attempt ──────────────────────────
        otpStorage.remove(email);
        otpExpiry.remove(email);

        if (storedOtp == null || expiry == null) {
            System.out.println("❌ OTP not found for: " + email);
            return false;
        }

        if (now > expiry) {
            System.out.println("❌ OTP expired for: " + email);
            return false;
        }

        if (!storedOtp.equals(otpInput)) {
            System.out.println("❌ Wrong OTP entered for: " + email);
            return false;
        }

        System.out.println("✅ OTP verified for: " + email);
        return true;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // Magic Link Methods
    // ════════════════════════════════════════════════════════════════════════════

    public void generateAndSendMagicLink(String email) throws MessagingException {
        String token = UUID.randomUUID().toString();

        magicLinkStorage.put(token, email);
        magicLinkExpiry.put(token, System.currentTimeMillis() + MAGIC_LINK_EXPIRY_MS);

        emailService.sendMagicLink(email, token);
        System.out.println("✅ Magic link sent to: " + email + " | Expires in 15 minutes.");
    }

    public String validateMagicToken(String token) {
        String email = magicLinkStorage.get(token);
        Long expiry = magicLinkExpiry.get(token);
        long now = System.currentTimeMillis();

        // ── Always clean up — single use only ────────────────────────────────
        magicLinkStorage.remove(token);
        magicLinkExpiry.remove(token);

        if (email == null || expiry == null) {
            System.out.println("❌ Magic token not found.");
            return null;
        }

        if (now > expiry) {
            System.out.println("❌ Magic token expired.");
            return null;
        }

        System.out.println("✅ Magic link verified for: " + email);
        return email;
    }
}