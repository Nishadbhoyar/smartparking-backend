package com.smartparking.dtos.request;

import com.smartparking.entities.nums.PromoType;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * REPLACE existing CreatePromoRequestDTO.java
 *
 * Example — New user only promo:
 * {
 *   "code": "WELCOME100",
 *   "type": "FLAT",
 *   "discountValue": 100.0,
 *   "minBookingAmount": 150.0,
 *   "maxUses": 1000,
 *   "newUsersOnly": true,
 *   "expiryDate": "2026-12-31T23:59:59"
 * }
 *
 * Example — Promo for all users:
 * {
 *   "code": "WEEKEND20",
 *   "type": "PERCENT",
 *   "discountValue": 20.0,
 *   "maxDiscountAmount": 100.0,
 *   "newUsersOnly": false,
 *   ...
 * }
 */
@Data
public class CreatePromoRequestDTO {
    private String        code;
    private PromoType     type;
    private Double        discountValue;
    private Double        maxDiscountAmount;
    private Double        minBookingAmount;
    private Integer       maxUses;
    private boolean       newUsersOnly;       // ← NEW
    private LocalDateTime expiryDate;
}