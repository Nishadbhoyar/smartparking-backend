package com.smartparking.repositories;

import com.smartparking.entities.rental.RentalCar;
import com.smartparking.entities.nums.RentalCarStatus;
import com.smartparking.entities.nums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RentalCarRepository extends JpaRepository<RentalCar, Long> {

    List<RentalCar> findByCarOwnerId(Long carOwnerId);
    List<RentalCar> findByRentalCompanyId(Long companyId);
    List<RentalCar> findByStatus(RentalCarStatus status);

    // Nearby available cars — no type filter
    @Query(value = "SELECT * FROM rental_cars r WHERE r.status = 'AVAILABLE' " +
            "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(r.pickup_latitude)) * " +
            "cos(radians(r.pickup_longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(r.pickup_latitude)))) ASC LIMIT :limit",
            nativeQuery = true)
    List<RentalCar> findNearbyAvailableCars(@Param("lat") double lat,
                                            @Param("lng") double lng,
                                            @Param("limit") int limit);

    // Nearby available cars — filtered by vehicle type
    @Query(value = "SELECT * FROM rental_cars r WHERE r.status = 'AVAILABLE' " +
            "AND r.vehicle_type = :vehicleType " +
            "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(r.pickup_latitude)) * " +
            "cos(radians(r.pickup_longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(r.pickup_latitude)))) ASC LIMIT :limit",
            nativeQuery = true)
    List<RentalCar> findNearbyAvailableCarsByType(@Param("lat") double lat,
                                                  @Param("lng") double lng,
                                                  @Param("vehicleType") String vehicleType,
                                                  @Param("limit") int limit);
}
