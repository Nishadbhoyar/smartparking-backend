package com.smartparking.dtos.response;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PlatformDashboardResponseDTO {

    // ── Platform totals ───────────────────────────────────────────────────
    private Long totalUsers;
    private Long totalParkingLots;

    // ── Revenue ───────────────────────────────────────────────────────────
    private Double totalRevenueToday;
    private Double totalRevenueThisWeek;
    private Double totalRevenueThisMonth;
    private Double totalRevenueAllTime;

    // ── Bookings ──────────────────────────────────────────────────────────
    private Long totalBookingsToday;
    private Long totalActiveBookings;
    private Long totalCompletedBookings;

    // ── Valet ─────────────────────────────────────────────────────────────
    private Long totalValetRequestsToday;
    private Long activeValets;

    // ── Slots ─────────────────────────────────────────────────────────────
    private Long totalSlots;
    private Long occupiedSlots;
    private Long availableSlots;

    // ── Top performers ────────────────────────────────────────────────────
    private List<TopLotDTO>   topParkingLots;
    private List<TopValetDTO> topValets;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TopLotDTO {
        private Long   lotId;
        private String lotName;
        private Double revenue;
        private Long   bookings;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TopValetDTO {
        private Long   valetId;
        private String valetName;
        private Long   jobsCompleted;
        private Double totalEarned;
    }
}