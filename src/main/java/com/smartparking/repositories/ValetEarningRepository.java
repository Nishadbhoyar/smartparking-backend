package com.smartparking.repositories;

import com.smartparking.entities.valet.ValetEarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ValetEarningRepository extends JpaRepository<ValetEarning, Long> {

    List<ValetEarning> findByValetIdOrderByEarnedAtDesc(Long valetId);

    List<ValetEarning> findByValetIdAndPaidFalse(Long valetId);

    @Query("SELECT SUM(e.valetCut) FROM ValetEarning e " +
            "WHERE e.valet.id = :valetId " +
            "AND e.earnedAt BETWEEN :start AND :end")
    Double sumEarningsByDateRange(@Param("valetId") Long valetId,
                                  @Param("start")   LocalDateTime start,
                                  @Param("end")     LocalDateTime end);

    @Query("SELECT SUM(e.valetCut) FROM ValetEarning e " +
            "WHERE e.valet.id = :valetId AND e.paid = false")
    Double sumUnpaidEarnings(@Param("valetId") Long valetId);

    @Query("SELECT SUM(e.valetCut) FROM ValetEarning e WHERE e.valet.id = :valetId")
    Double sumTotalEarningsByValetId(@Param("valetId") Long valetId);

    Optional<ValetEarning> findByValetRequestId(Long valetRequestId);

    // FIX #8: Added so ValetEarningsServiceImpl can count completed jobs correctly
    long countByValetId(Long valetId);

    @Query("SELECT e.valet.id, e.valet.name, COUNT(e), SUM(e.valetCut) " +
            "FROM ValetEarning e " +
            "GROUP BY e.valet.id, e.valet.name " +
            "ORDER BY SUM(e.valetCut) DESC")
    List<Object[]> findTopValetsByEarnings();
}