package com.smartparking.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * M-04 FIX: Persists FCM device tokens so push notifications can actually be sent.
 * Previously NotificationController acknowledged registration but discarded the token.
 */
@Entity
@Table(name = "device_tokens",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "device_token"}))
@Data
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_token", nullable = false, length = 512)
    private String token;

    @Column(name = "device_type", length = 20)
    private String deviceType; // ANDROID, IOS, WEB

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @PrePersist
    public void prePersist() {
        registeredAt = LocalDateTime.now();
    }
}