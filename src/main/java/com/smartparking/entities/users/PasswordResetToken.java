package com.smartparking.entities.users;

import com.smartparking.entities.users.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 6-digit OTP sent to the user's email
    @Column(nullable = false)
    private String otp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // OTP is valid for 15 minutes
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private boolean used = false;
}