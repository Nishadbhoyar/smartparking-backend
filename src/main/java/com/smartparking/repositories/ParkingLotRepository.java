package com.smartparking.repositories;

import com.smartparking.entities.nums.ParkingLotStatus;
import com.smartparking.entities.parking.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {

    List<ParkingLot> findByStatus(ParkingLotStatus status);

    // ✅ FIXED: was findByAdminId — renamed to match the new field name
    List<ParkingLot> findByParkingLotAdminId(Long parkingLotAdminId);

    // ✅ FIXED: was findByIdAndAdminId — renamed to match the new field name
    Optional<ParkingLot> findByIdAndParkingLotAdminId(Long lotId, Long parkingLotAdminId);

    @Query(value = "SELECT * FROM parking_lots p ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(p.latitude)) * cos(radians(p.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(p.latitude)))) ASC LIMIT 5", nativeQuery = true)
    List<ParkingLot> findNearbyParkingLots(@Param("lat") double lat, @Param("lng") double lng);

    @Query(value = "SELECT * FROM parking_lots pl WHERE pl.status = 'ACTIVE' " +
            "ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(pl.latitude)) * " +
            "cos(radians(pl.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(pl.latitude)))) ASC LIMIT :limit",
            nativeQuery = true)
    List<ParkingLot> findNearbyActiveLots(@Param("latitude") double latitude,
                                          @Param("longitude") double longitude,
                                          @Param("limit") int limit);
}