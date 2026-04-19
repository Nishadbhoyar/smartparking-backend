package com.smartparking.entities.featuresentites;

import com.smartparking.entities.nums.PromoType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * REPLACE existing PromoCode.java
 * Location: com/smartparking/entities/PromoCode.java
 *
 * Change: Added `newUsersOnly` flag
 * If true → only customers with 0 previous completed bookings can use this code
 */
@Entity
@Table(name = "promo_codes")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    private PromoType type;

    private Double  discountValue;
    private Double  maxDiscountAmount;
    private Double  minBookingAmount;
    private Integer maxUses;
    private Integer usedCount;

    // NEW: if true → only customers with 0 previous bookings can use this
    private boolean newUsersOnly;

    private LocalDateTime expiryDate;
    private boolean isActive;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.usedCount = 0;
        this.isActive  = true;
    }
}