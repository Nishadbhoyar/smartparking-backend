package com.smartparking.backend.repository;

import com.smartparking.backend.model.Otptoken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtptokenRepository extends JpaRepository<Otptoken, Long> {

    // Find OTP record by email and type (e.g. "OTP" or "MAGIC_LINK")
    Optional<Otptoken> findByEmailAndType(String email, String type);

    // Find magic link by token value
    Optional<Otptoken> findByTokenAndType(String token, String type);

    // Delete all expired tokens — called periodically to keep table clean
    @Modifying
    @Transactional
    @Query("DELETE FROM OtpToken o WHERE o.expiresAt < :now")
    void deleteAllExpiredTokens(LocalDateTime now);
}