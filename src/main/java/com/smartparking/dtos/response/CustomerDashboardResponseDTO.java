package com.smartparking.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDashboardResponseDTO {

    // Active parking/booking session (null if none)
    private ActiveBookingDTO activeBooking;

    // Financial summary
    private Double totalSpent;

    // Usage summary
    private Long totalBookings;
    private String favouriteLot;

    // Recent bookings (last 5)
    private List<RecentBookingDTO> recentBookings;

    // ── Nested DTOs ───────────────────────────────────────────────────────

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActiveBookingDTO {
        private Long bookingId;
        private String bookingCode;
        private String lotName;
        private String slotNumber;
        private LocalDateTime entryTime;
        private String status;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentBookingDTO {
        private Long bookingId;
        private String bookingCode;
        private String lotName;
        private LocalDateTime entryTime;
        private LocalDateTime exitTime;
        private Double totalAmount;
        private String status;
    }
}