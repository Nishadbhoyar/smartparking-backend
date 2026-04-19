package com.smartparking.dtos.response;

import lombok.*;

/**
 * FILE 7 of 8
 * Location: com/smartparking/dtos/response/FareResponseDTO.java
 *
 * Sent to the frontend to display the fare breakdown to the customer.
 *
 * Example UI display:
 * ──────────────────────────────────
 *  Fare Breakdown
 *  Base Fare             ₹  50.00
 *  Distance  (7.4 km)    ₹  88.80
 *  Parking   (2.5 hrs)   ₹  75.00
 * ──────────────────────────────────
 *  Subtotal              ₹ 213.80
 *  Surge (1.5×) 🔴       ₹ 106.90
 * ──────────────────────────────────
 *  TOTAL                 ₹ 320.70
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FareResponseDTO {

    private Long    fareId;
    private Long    valetRequestId;

    // ── Distance breakdown ────────────────────────────────────────────────
    private double  pickupDistanceKm;       // valet → customer
    private double  parkingDistanceKm;      // customer → lot
    private double  returnDistanceKm;       // lot → customer
    private double  totalDistanceKm;        // sum of all 3

    // ── Fare breakdown (₹) ───────────────────────────────────────────────
    private double  baseFare;               // ₹50 flat
    private double  distanceFare;           // ₹12 × total km
    private double  parkingFare;            // ₹30 × hours
    private double  surgeFare;              // extra surge amount (0 if no surge)
    private double  totalFare;              // final amount

    // ── Parking duration ─────────────────────────────────────────────────
    private double  hoursParked;
    private boolean isEstimate;             // true = estimate shown before job starts

    // ── Surge info ───────────────────────────────────────────────────────
    private boolean isSurge;
    private double  surgeMultiplier;        // 1.0 = normal, 1.5 = peak

    // ── Payment ───────────────────────────────────────────────────────────
    private String  paymentStatus;          // PENDING / CALCULATED / PAID

    // ── ETA info (shown on booking screen) ───────────────────────────────
    private Integer valetEtaMinutes;        // how long until valet arrives
    private String  nearestLotName;         // which lot car will be parked at
    private double  nearestLotDistanceKm;   // how far the lot is from customer
}