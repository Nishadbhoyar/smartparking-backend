package com.smartparking.repositories;

import com.smartparking.entities.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * M-04 FIX: Repository for device token storage.
 */
@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    List<DeviceToken> findByUserId(Long userId);
    void deleteByUserIdAndToken(Long userId, String token);
    boolean existsByUserIdAndToken(Long userId, String token);
}