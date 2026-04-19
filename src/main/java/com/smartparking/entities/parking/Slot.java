package com.smartparking.entities.parking;

import com.smartparking.entities.nums.SlotStatus;
import com.smartparking.entities.nums.SlotType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
// This unique constraint guarantees the database will NEVER allow two "A1"s in Lot 1
@Table(name = "slots", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"slot_number", "parking_lot_id"})
})
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slot_number", nullable = false)
    private String slotNumber;

    // NEW: To support grouping on the Map View (e.g., "Zone A")
    private String zone;

    // NEW: Replaced the boolean with the Enum
    // FIX: Added length=20 to accommodate all enum values
    // SlotStatus enum values are relatively short, but we specify 20 for consistency
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SlotStatus status = SlotStatus.AVAILABLE;

    // FIX: Added length=20 to accommodate the longest enum value "HEAVY_VEHICLE" (12 chars)
    // Enum values: REGULAR(7), EV_CHARGING(11), DISABLED(8), HEAVY_VEHICLE(12),
    //              BIKE(4), CAR(3), TRUCK(5), BUS(3), SUV(3), EV(2), HANDICAPPED(11), VIP(3)
    // Length=20 provides buffer for future enum additions
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SlotType slotType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;

    private double hourlyRate;
}