package com.smartparking.dtos.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingRequestDTO {
    private Long customerId;
    private Long parkingLotId;
    private String slotType;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private boolean isValetBooking;

    // C-3 / C-4 FIX: totalAmount removed — backend recalculates it.
    // promoCode added — backend applies discount atomically and records usage.
    private String promoCode;   // optional, null means no promo
}
