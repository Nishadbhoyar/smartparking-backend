package com.smartparking.dtos.request;

import com.smartparking.entities.nums.SlotType;
import lombok.Data;

/**
 * Accepts both the old multi-count format and the new single-type format
 * sent by the frontend SlotManagerPage.
 *
 * Frontend sends:
 * {
 *   "lotId": 1,
 *   "count": 10,
 *   "vehicleType": "CAR",
 *   "floor": "G",
 *   "namePrefix": "A",
 *   "rows": 2,
 *   "cols": 6
 * }
 *
 * Old Postman format still works:
 * {
 *   "parkingLotId": 1,
 *   "regularCount": 5,
 *   "evCount": 3,
 *   "bikeCount": 2,
 *   "heavyVehicleCount": 1,
 *   "defaultHourlyRate": 50
 * }
 */
@Data
public class BulkSlotRequestDTO {

    // ── New frontend format ──────────────────────────────────────────────
    // Frontend sends "lotId" — this is the primary field
    private Long lotId;

    // Single type + count mode (frontend default)
    private Integer count;
    private SlotType vehicleType;   // e.g. CAR, BIKE, EV, TRUCK, BUS, SUV, VIP, HANDICAPPED
    private String floor;           // G, B1, B2, 1, 2, 3...
    private String namePrefix;      // e.g. "A" → A1, A2, A3...

    // ── Legacy Postman / API format ──────────────────────────────────────
    // "parkingLotId" is the old field — we merge both in the service
    private Long parkingLotId;

    private Integer regularCount;
    private Integer evCount;
    private Integer heavyVehicleCount;
    private Integer bikeCount;
    private double  defaultHourlyRate;

    // rows/cols sent by frontend — kept for compatibility but not used
    private Integer rows;
    private Integer cols;

    /**
     * Returns the effective parking lot ID.
     * Accepts either "lotId" (frontend) or "parkingLotId" (legacy).
     */
    public Long getEffectiveLotId() {
        if (lotId != null)        return lotId;
        if (parkingLotId != null) return parkingLotId;
        return null;
    }
}
