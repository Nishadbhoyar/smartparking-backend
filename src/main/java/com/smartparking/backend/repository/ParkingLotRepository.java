// package com.smartparking.backend.repository;

// import com.smartparking.backend.model.ParkingLot;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository;

// @Repository
// public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
//     // ✅ ONLY BASIC CRUD
//     // ❌ REMOVED findByCity
//     // ❌ REMOVED findByOwnerId
// }
// }

package com.smartparking.backend.repository;

import com.smartparking.backend.model.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {

    // Basic CRUD operations are inherited from JpaRepository

    // Find parking lots by name (case-insensitive search)
    List<ParkingLot> findByNameContainingIgnoreCase(String name);

    // Find parking lots by address
    List<ParkingLot> findByAddressContainingIgnoreCase(String address);

    // Custom query to find parking lots within a radius (using Haversine formula)
    @Query("SELECT p FROM ParkingLot p WHERE " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(p.latitude)) * " +
            "cos(radians(p.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(p.latitude)))) < :radiusKm")
    List<ParkingLot> findNearbyParkingLots(Double latitude, Double longitude, Double radiusKm);

    List<ParkingLot> findByOwnerId(Long ownerId);
}
