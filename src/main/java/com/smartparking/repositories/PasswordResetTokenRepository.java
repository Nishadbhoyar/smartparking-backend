package com.smartparking.repositories;


import com.smartparking.entities.users.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // Find the latest unused token for a user by OTP
    Optional<PasswordResetToken> findByOtpAndUsedFalse(String otp);

    // Delete all previous tokens for a user before issuing a new one
    void deleteByUserId(Long userId);
}