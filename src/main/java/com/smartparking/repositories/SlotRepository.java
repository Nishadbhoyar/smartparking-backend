package com.smartparking.repositories;

import com.smartparking.entities.parking.Slot;
import com.smartparking.entities.nums.SlotStatus;
import com.smartparking.entities.nums.SlotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {

    List<Slot> findByParkingLotId(Long parkingLotId);

    // NEW: Checks if a slot name already exists in a specific lot!
    boolean existsBySlotNumberAndParkingLotId(String slotNumber, Long parkingLotId);

    // UPDATED: Now uses the Enum
    long countByParkingLotIdAndStatus(Long parkingLotId, SlotStatus status);

    // UPDATED: Now uses the Enum
    Optional<Slot> findFirstByParkingLotIdAndStatusAndSlotType(Long parkingLotId, SlotStatus status, SlotType slotType);

    // Counts all slots regardless of status
    long countByParkingLotId(Long parkingLotId);

    Optional<Slot> findFirstByParkingLotIdAndStatus(Long parkingLotId, SlotStatus status);

    Long countByStatus(SlotStatus status);

    long countByParkingLotIdAndSlotType(Long parkingLotId, com.smartparking.entities.nums.SlotType slotType);
}