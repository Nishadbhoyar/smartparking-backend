package com.smartparking.backend.controller;

import com.smartparking.backend.model.Booking;
import com.smartparking.backend.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/stats")
@CrossOrigin(origins = "*")
public class AdminStatsController {

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats(@RequestParam Long adminId) {
        // In a real app, you'd filter by lots owned by adminId.
        // For now, we'll grab all completed bookings for the chart.
        List<Booking> allBookings = bookingRepository.findAll();

        double thisWeekRevenue = 0;
        double lastWeekRevenue = 0;

        // Setup 7-day chart data
        Map<String, Double> chartMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            chartMap.put(LocalDateTime.now().minusDays(i).getDayOfWeek().name().substring(0, 3), 0.0);
        }

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusDays(14);

        for (Booking b : allBookings) {
            if (b.getTotalAmount() != null && "COMPLETED".equals(b.getStatus())) {
                if (b.getEndTime() != null && b.getEndTime().isAfter(oneWeekAgo)) {
                    thisWeekRevenue += b.getTotalAmount();
                    String day = b.getEndTime().getDayOfWeek().name().substring(0, 3);
                    chartMap.put(day, chartMap.getOrDefault(day, 0.0) + b.getTotalAmount());
                } else if (b.getEndTime() != null && b.getEndTime().isAfter(twoWeeksAgo)) {
                    lastWeekRevenue += b.getTotalAmount();
                }
            }
        }

        double growth = lastWeekRevenue > 0 ? ((thisWeekRevenue - lastWeekRevenue) / lastWeekRevenue) * 100
                : (thisWeekRevenue > 0 ? 100.0 : 0.0);

        // Convert map to list of objects for React Recharts
        List<Map<String, Object>> chartData = new ArrayList<>();
        for (Map.Entry<String, Double> entry : chartMap.entrySet()) {
            chartData.add(Map.of("name", entry.getKey(), "revenue", entry.getValue()));
        }

        return ResponseEntity.ok(Map.of(
                "growthPercentage", Math.round(growth * 10.0) / 10.0,
                "chartData", chartData));
    }
}