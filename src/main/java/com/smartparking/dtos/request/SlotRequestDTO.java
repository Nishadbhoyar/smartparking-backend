package com.smartparking.dtos.request;

import com.smartparking.entities.nums.SlotType;
import lombok.Data;

/**
 * Accepts both frontend format and legacy Postman format.
 *
 * Frontend sends:
 * { "lotId": 1, "name": "A1", "type": "CAR", "floor": "G" }
 *
 * Legacy Postman format:
 * { "parkingLotId": 1, "slotNumber": "A1", "slotType": "REGULAR", "zone": "A", "hourlyRate": 50 }
 */
@Data
public class SlotRequestDTO {

    // ── Frontend field names ─────────────────────────────────────────────
    private Long    lotId;       // frontend sends "lotId"
    private String  name;        // frontend sends "name"
    private SlotType type;       // frontend sends "type"
    private String  floor;       // frontend sends "floor"

    // ── Legacy / Postman field names ─────────────────────────────────────
    private Long     parkingLotId;
    private String   slotNumber;
    private SlotType slotType;
    private String   zone;
    private double   hourlyRate;

    // ── Resolved getters — always return the correct value ───────────────

    /** Returns the effective lot ID — accepts either "lotId" or "parkingLotId" */
    public Long getEffectiveLotId() {
        if (lotId != null)        return lotId;
        if (parkingLotId != null) return parkingLotId;
        return null;
    }

    /** Returns the effective slot name — accepts "name" or "slotNumber" */
    public String getEffectiveSlotNumber() {
        if (name != null && !name.isBlank())             return name;
        if (slotNumber != null && !slotNumber.isBlank()) return slotNumber;
        return null;
    }

    /** Returns the effective slot type — accepts "type" or "slotType" */
    public SlotType getEffectiveSlotType() {
        if (type != null)     return type;
        if (slotType != null) return slotType;
        return com.smartparking.entities.nums.SlotType.REGULAR; // safe default
    }

    /** Returns zone — accepts "floor" or "zone" */
    public String getEffectiveZone() {
        if (floor != null && !floor.isBlank()) return floor;
        if (zone  != null && !zone.isBlank())  return zone;
        return "G"; // default Ground floor
    }
}