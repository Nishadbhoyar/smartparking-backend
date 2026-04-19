package com.smartparking.repositories;

import com.smartparking.entities.Booking;
import com.smartparking.entities.nums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ── EXISTING (unchanged — kept your exact query style) ────────────────

    List<Booking> findByCustomerIdOrderByEntryTimeDesc(Long customerId);

    Optional<Booking> findByBookingCode(String bookingCode);

    // M-03 FIX: Used in uniqueness retry loop during booking creation
    boolean existsByBookingCode(String bookingCode);

    List<Booking> findByParkingLotIdOrderByEntryTimeDesc(Long parkingLotId);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0.0) FROM Booking b " +
            "WHERE b.parkingLot.id = :lotId AND b.status = :status " +
            "AND b.exitTime >= :startOfDay AND b.exitTime <= :endOfDay")
    Double calculateEarningsByDateRange(@Param("lotId")      Long lotId,
                                        @Param("status")     BookingStatus status,
                                        @Param("startOfDay") LocalDateTime startOfDay,
                                        @Param("endOfDay")   LocalDateTime endOfDay);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0.0) FROM Booking b " +
            "WHERE b.parkingLot.id = :lotId AND b.status = :status")
    Double calculateTotalRevenue(@Param("lotId")  Long lotId,
                                 @Param("status") BookingStatus status);

    long countByParkingLotIdAndStatus(Long parkingLotId, BookingStatus status);

    // ── NEW: PromoServiceImpl — new-user promo check ──────────────────────

    long countByCustomerIdAndStatus(Long customerId, BookingStatus status);

    // ── NEW: AnalyticsServiceImpl — platform-wide dashboard ───────────────

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0.0) FROM Booking b " +
            "WHERE b.status = :status")
    Double calculatePlatformTotalRevenue(@Param("status") BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0.0) FROM Booking b " +
            "WHERE b.status = :status " +
            "AND b.exitTime >= :start AND b.exitTime <= :end")
    Double calculatePlatformEarningsByDateRange(@Param("status") BookingStatus status,
                                                @Param("start")  LocalDateTime start,
                                                @Param("end")    LocalDateTime end);

    long countByStatusAndEntryTimeBetween(BookingStatus status,
                                          LocalDateTime start,
                                          LocalDateTime end);

    long countByStatus(BookingStatus status);

    // Returns [lotId, lotName, revenue, bookingCount] ordered by revenue desc
    @Query("SELECT b.parkingLot.id, b.parkingLot.name, " +
            "COALESCE(SUM(b.totalAmount), 0.0), COUNT(b) " +
            "FROM Booking b WHERE b.status = :status " +
            "GROUP BY b.parkingLot.id, b.parkingLot.name " +
            "ORDER BY SUM(b.totalAmount) DESC")
    List<Object[]> findTopLotsByRevenue(@Param("status") BookingStatus status);

    // ── NEW: CustomerDashboardServiceImpl ─────────────────────────────────

    Optional<Booking> findFirstByCustomerIdAndStatus(Long customerId,
                                                     BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0.0) FROM Booking b " +
            "WHERE b.customer.id = :customerId AND b.status = :status")
    Double sumTotalAmountByCustomerIdAndStatus(@Param("customerId") Long customerId,
                                               @Param("status")     BookingStatus status);

    @Query("SELECT b.parkingLot.name FROM Booking b " +
            "WHERE b.customer.id = :customerId " +
            "GROUP BY b.parkingLot.id, b.parkingLot.name " +
            "ORDER BY COUNT(b) DESC " +
            "LIMIT 1")
    Optional<String> findFavouriteLotNameByCustomerId(@Param("customerId") Long customerId);

    List<Booking> findTop5ByCustomerIdOrderByEntryTimeDesc(Long customerId);

    long countByCustomerId(Long customerId);

    // ── NEW: LotAdminDashboardServiceImpl — multi-lot queries ─────────────

    long countByParkingLotIdIn(List<Long> lotIds);

    long countByParkingLotIdInAndStatus(List<Long> lotIds, BookingStatus status);

    long countByParkingLotIdInAndStatusAndEntryTimeBetween(List<Long> lotIds,
                                                           BookingStatus status,
                                                           LocalDateTime start,
                                                           LocalDateTime end);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0.0) FROM Booking b " +
            "WHERE b.parkingLot.id IN :lotIds AND b.status = :status " +
            "AND b.exitTime >= :start AND b.exitTime <= :end")
    Double calculateEarningsByLotIdsAndDateRange(@Param("lotIds")  List<Long> lotIds,
                                                 @Param("status") BookingStatus status,
                                                 @Param("start")  LocalDateTime start,
                                                 @Param("end")    LocalDateTime end);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0.0) FROM Booking b " +
            "WHERE b.parkingLot.id IN :lotIds AND b.status = :status")
    Double calculateTotalRevenueByLotIds(@Param("lotIds")  List<Long> lotIds,
                                         @Param("status") BookingStatus status);

    List<Booking> findTop10ByParkingLotIdInOrderByEntryTimeDesc(List<Long> lotIds);
}