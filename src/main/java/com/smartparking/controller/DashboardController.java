package com.smartparking.controller;

import com.smartparking.dtos.response.CustomerDashboardResponseDTO;
import com.smartparking.entities.Booking;
import com.smartparking.entities.nums.BookingStatus;
import com.smartparking.repositories.BookingRepository;
import com.smartparking.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // FIX #3: Added @Transactional(readOnly = true) — without this the lazy
    // parkingLot and slot associations on Booking throw LazyInitializationException
    // when accessed inside the stream below, because the session closes after the
    // repository call returns.
    @GetMapping("/customer/{customerId}")
    @Transactional(readOnly = true)
    public ResponseEntity<CustomerDashboardResponseDTO> getCustomerDashboard(
            @PathVariable Long customerId) {

        // 1. Active booking (ACTIVE or PENDING status)
        CustomerDashboardResponseDTO.ActiveBookingDTO activeBookingDTO = null;

        Optional<Booking> activeBooking =
                bookingRepository.findFirstByCustomerIdAndStatus(customerId, BookingStatus.ACTIVE);

        if (activeBooking.isEmpty()) {
            activeBooking = bookingRepository.findFirstByCustomerIdAndStatus(
                    customerId, BookingStatus.PENDING);
        }

        if (activeBooking.isPresent()) {
            Booking b = activeBooking.get();
            activeBookingDTO = new CustomerDashboardResponseDTO.ActiveBookingDTO(
                    b.getId(),
                    b.getBookingCode(),
                    b.getParkingLot().getName(),       // lazy — needs open session
                    b.getSlot() != null ? b.getSlot().getSlotNumber() : null, // lazy — needs open session
                    b.getEntryTime(),
                    b.getStatus().name()
            );
        }

        // 2. Total spent (COMPLETED bookings only)
        Double totalSpent = bookingRepository
                .sumTotalAmountByCustomerIdAndStatus(customerId, BookingStatus.COMPLETED);
        if (totalSpent == null) totalSpent = 0.0;

        // 3. Total booking count
        long totalBookings = bookingRepository.countByCustomerId(customerId);

        // 4. Favourite lot
        String favouriteLot = bookingRepository
                .findFavouriteLotNameByCustomerId(customerId)
                .orElse(null);

        // 5. Recent 5 bookings
        List<Booking> recent = bookingRepository
                .findTop5ByCustomerIdOrderByEntryTimeDesc(customerId);

        List<CustomerDashboardResponseDTO.RecentBookingDTO> recentDTOs = recent.stream()
                .map(b -> new CustomerDashboardResponseDTO.RecentBookingDTO(
                        b.getId(),
                        b.getBookingCode(),
                        b.getParkingLot().getName(), // lazy — needs open session
                        b.getEntryTime(),
                        b.getExitTime(),
                        b.getTotalAmount(),
                        b.getStatus().name()
                ))
                .collect(Collectors.toList());

        CustomerDashboardResponseDTO response = new CustomerDashboardResponseDTO(
                activeBookingDTO,
                totalSpent,
                totalBookings,
                favouriteLot,
                recentDTOs
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/{adminId}")
    public ResponseEntity<?> getLotAdminDashboard(@PathVariable Long adminId) {
        return ResponseEntity.ok(java.util.Map.of("message", "Admin dashboard for " + adminId));
    }

    @GetMapping("/valet/{valetId}")
    public ResponseEntity<?> getValetDashboard(@PathVariable Long valetId) {
        return ResponseEntity.ok(java.util.Map.of("message", "Valet dashboard for " + valetId));
    }
}