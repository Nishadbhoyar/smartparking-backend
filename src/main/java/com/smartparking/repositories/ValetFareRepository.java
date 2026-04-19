package com.smartparking.repositories;


import com.smartparking.entities.valet.ValetFare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * FILE 6 of 8
 * Location: com/smartparking/repositories/ValetFareRepository.java
 *
 * Repository for ValetFare entity.
 * Used by FareCalculationService to save and retrieve fare records.
 */
public interface ValetFareRepository extends JpaRepository<ValetFare, Long> {

    // Find the fare record for a specific valet request
    Optional<ValetFare> findByValetRequestId(Long valetRequestId);
}