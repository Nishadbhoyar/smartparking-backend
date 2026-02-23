package com.smartparking.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_tokens")
public class Otptoken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The email this OTP/token belongs to
    @Column(nullable = false)
    private String email;

    // The actual OTP code (6 digits) or magic link UUID token
    @Column(nullable = false)
    private String token;

    // "OTP" or "MAGIC_LINK"
    @Column(nullable = false)
    private String type;

    // When this OTP/link expires
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // Track how many OTPs this email requested in the current window (for rate limiting)
    @Column(nullable = false)
    private int requestCount = 0;

    // When the rate limit window started
    private LocalDateTime rateLimitWindowStart;

    // ─── Constructors ─────────────────────────────────────────────────────────

    public Otptoken() {}

    public Otptoken(String email, String token, String type, LocalDateTime expiresAt) {
        this.email     = email;
        this.token     = token;
        this.type      = type;
        this.expiresAt = expiresAt;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public int getRequestCount() { return requestCount; }
    public void setRequestCount(int requestCount) { this.requestCount = requestCount; }

    public LocalDateTime getRateLimitWindowStart() { return rateLimitWindowStart; }
    public void setRateLimitWindowStart(LocalDateTime rateLimitWindowStart) { this.rateLimitWindowStart = rateLimitWindowStart; }
}