package com.smartparking.backend.repository;

import com.smartparking.backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List; // ✅ REQUIRED

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStatus(String status);

    List<Booking> findByUser_Id(Long userId);

    List<Booking> findByLot_Id(Long lotId);

    List<Booking> findByValet_Id(Long valetId);

    List<Booking> findByStatusAndValet_Id(String status, Long valetId);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.lot.ownerId = :ownerId")
    Double calculateTotalEarningsForAdmin(Long ownerId);

}
