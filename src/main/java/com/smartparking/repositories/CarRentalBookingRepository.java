package com.smartparking.repositories;

import com.smartparking.entities.rental.CarRentalBooking;
import com.smartparking.entities.nums.CarRentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CarRentalBookingRepository extends JpaRepository<CarRentalBooking, Long> {

    List<CarRentalBooking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<CarRentalBooking> findByRentalCarIdOrderByCreatedAtDesc(Long rentalCarId);
    Optional<CarRentalBooking> findByBookingCode(String bookingCode);
    List<CarRentalBooking> findByRentalCarIdAndStatus(Long rentalCarId, CarRentalStatus status);

    // Find all ACTIVE bookings where endTime has passed and owner not yet notified
    List<CarRentalBooking> findByStatusAndEndTimeBeforeAndOverdueAlerted(
            CarRentalStatus status,
            LocalDateTime now,
            Boolean overdueAlerted
    );

    // Check if car has any CONFIRMED or ACTIVE booking overlapping a time window
    // Used to validate extension requests won't conflict with the next booking
    @Query("SELECT COUNT(b) > 0 FROM CarRentalBooking b " +
            "WHERE b.rentalCar.id = :carId " +
            "AND b.id != :excludeBookingId " +
            "AND b.status IN ('CONFIRMED', 'ACTIVE') " +
            "AND b.startTime < :newEndTime " +
            "AND b.endTime > :newStartTime")
    boolean hasOverlappingBooking(
            @Param("carId")            Long carId,
            @Param("excludeBookingId") Long excludeBookingId,
            @Param("newStartTime")     LocalDateTime newStartTime,
            @Param("newEndTime")       LocalDateTime newEndTime
    );
}
