// ══════════════════════════════════════════════════════════════
// 1. NEW FILE: src/main/java/com/smartparking/entities/NotificationHistory.java
// ══════════════════════════════════════════════════════════════
package com.smartparking.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Stores a log of every in-app notification sent to a user.
 * This powers the NotificationsPage in the frontend.
 */
@Entity
@Data
@Table(name = "notification_history")
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 500)
    private String body;

    // e.g. BOOKING, VALET, PROMO, SYSTEM
    private String type;

    // Whether the user has seen it
    @Column (name="is_read",nullable = false)
    private boolean read;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}


// ══════════════════════════════════════════════════════════════
// 2. NEW FILE: src/main/java/com/smartparking/repositories/NotificationHistoryRepository.java
// ══════════════════════════════════════════════════════════════
// package com.smartparking.repositories;
//
// import com.smartparking.entities.NotificationHistory;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository;
// import java.util.List;
//
// @Repository
// public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {
//     List<NotificationHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
//     long countByUserIdAndReadFalse(Long userId);
//     void deleteByUserId(Long userId);
// }