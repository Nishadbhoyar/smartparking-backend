package com.smartparking.repositories;

import com.smartparking.entities.valet.Valet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ValetRepository extends JpaRepository<Valet, Long> {

    @Query("SELECT v FROM Valet v WHERE v.isAvailableNow = true")
    List<Valet> findAllAvailableValets();

    @Modifying
    @Transactional
    @Query("UPDATE Valet v SET v.isAvailableNow = false WHERE v.id = :id")
    void markAsBusy(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Valet v SET v.isAvailableNow = true WHERE v.id = :id")
    void markAsFree(@Param("id") Long id);

    // FIX #7: available parameter was declared but silently ignored in the original query.
    // Now isAvailableNow is included so GPS pings also update availability correctly.
    @Modifying
    @Transactional
    @Query("UPDATE Valet v SET v.currentLatitude = :lat, v.currentLongitude = :lon, " +
            "v.isAvailableNow = :available WHERE v.id = :id")
    void updateLocation(@Param("id")        Long    id,
                        @Param("lat")       double  lat,
                        @Param("lon")       double  lon,
                        @Param("available") boolean available);

    Long countByIsAvailableNowTrue();
}