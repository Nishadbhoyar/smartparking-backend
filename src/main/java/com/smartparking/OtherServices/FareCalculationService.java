package com.smartparking.OtherServices;


import com.smartparking.entities.valet.ValetFare;
import com.smartparking.entities.valet.ValetRequest;
import com.smartparking.repositories.ValetFareRepository;
import com.smartparking.utils.GeoUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * FILE 4 of 8
 * Location: com/smartparking/service/FareCalculationService.java
 *
 * Calculates the full fare for every valet job.
 *
 * Fare breakdown:
 *   Base fare     → ₹50 flat fee per job
 *   Distance fare → ₹12 per km (total distance valet drives)
 *   Parking fare  → ₹30 per hour (how long car is stored)
 *   Surge fare    → 1.5× during peak hours (8-10 AM, 5-8 PM)
 */
@Service
public class FareCalculationService {

    // ── Pricing config ────────────────────────────────────────────────────
    private static final double BASE_FARE             = 50.0;   // ₹50 flat
    private static final double RATE_PER_KM           = 12.0;   // ₹12/km
    private static final double PARKING_RATE_PER_HOUR = 30.0;   // ₹30/hour
    private static final double MIN_PARKING_CHARGE    = 30.0;   // minimum ₹30 parking
    private static final double SURGE_MULTIPLIER      = 1.5;    // 1.5× surge

    // Peak hours → surge applies
    private static final LocalTime MORNING_SURGE_START = LocalTime.of(8, 0);
    private static final LocalTime MORNING_SURGE_END   = LocalTime.of(10, 0);
    private static final LocalTime EVENING_SURGE_START = LocalTime.of(17, 0);
    private static final LocalTime EVENING_SURGE_END   = LocalTime.of(20, 0);

    private final ValetFareRepository fareRepository;

    public FareCalculationService(ValetFareRepository fareRepository) {
        this.fareRepository = fareRepository;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  STEP 1 — Create fare estimate when job is accepted
    //  Assumes 3 hours parking as default estimate
    // ─────────────────────────────────────────────────────────────────────

    public ValetFare createFareEstimate(ValetRequest request,
                                        double valetLat, double valetLon,
                                        double lotLat, double lotLon) {

        double userLat = request.getPickupLatitude();
        double userLon = request.getPickupLongitude();

        // Valet → Customer
        double pickupDistKm  = GeoUtils.calculateDistanceKm(valetLat, valetLon, userLat, userLon);
        // Customer → Parking Lot
        double parkingDistKm = GeoUtils.calculateDistanceKm(userLat, userLon, lotLat, lotLon);
        // Parking Lot → Customer (return trip)
        double returnDistKm  = parkingDistKm;

        boolean isSurge = isSurgeTime(LocalDateTime.now());

        ValetFare fare = buildFare(
                request, pickupDistKm, parkingDistKm, returnDistKm,
                3.0,   // estimate 3 hours parking
                isSurge
        );

        return fareRepository.save(fare);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  STEP 2 — Finalize fare when car is returned to customer
    //  Uses actual hours the car was parked
    // ─────────────────────────────────────────────────────────────────────

    public ValetFare finalizeFare(Long requestId) {
        ValetFare fare = fareRepository.findByValetRequestId(requestId)
                .orElseThrow(() -> new RuntimeException(
                        "No fare record found for request: " + requestId
                ));

        LocalDateTime returnedAt = LocalDateTime.now();
        fare.setReturnedAt(returnedAt);

        // Calculate exact hours the car was parked
        double exactHours = 1.0; // minimum 1 hour
        if (fare.getParkedAt() != null) {
            long minutes = Duration.between(fare.getParkedAt(), returnedAt).toMinutes();
            exactHours = Math.max(minutes / 60.0, 1.0);
        }
        fare.setHoursParked(exactHours);

        // Recalculate parking fare with actual hours
        double parkingFare = Math.max(
                exactHours * PARKING_RATE_PER_HOUR,
                MIN_PARKING_CHARGE
        );
        fare.setParkingFare(round(parkingFare));

        // Recalculate total
        double raw   = fare.getBaseFare() + fare.getDistanceFare() + parkingFare;
        double total = fare.isSurge() ? raw * fare.getSurgeMultiplier() : raw;

        fare.setTotalFare(round(total));
        fare.setPaymentStatus(ValetFare.PaymentStatus.CALCULATED);

        return fareRepository.save(fare);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  STEP 3 — Mark as paid after payment confirmed
    // ─────────────────────────────────────────────────────────────────────

    public ValetFare markAsPaid(Long requestId) {
        ValetFare fare = fareRepository.findByValetRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Fare not found"));
        fare.setPaymentStatus(ValetFare.PaymentStatus.PAID);
        return fareRepository.save(fare);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Internal: build the fare object
    // ─────────────────────────────────────────────────────────────────────

    private ValetFare buildFare(ValetRequest request,
                                double pickupDistKm,
                                double parkingDistKm,
                                double returnDistKm,
                                double hoursParked,
                                boolean isSurge) {

        double totalDist    = pickupDistKm + parkingDistKm + returnDistKm;
        double distanceFare = totalDist * RATE_PER_KM;
        double parkingFare  = Math.max(hoursParked * PARKING_RATE_PER_HOUR, MIN_PARKING_CHARGE);
        double surgeMultiplier = isSurge ? SURGE_MULTIPLIER : 1.0;
        double raw   = BASE_FARE + distanceFare + parkingFare;
        double total = raw * surgeMultiplier;

        return ValetFare.builder()
                .valetRequest(request)
                .pickupDistanceKm(round(pickupDistKm))
                .parkingDistanceKm(round(parkingDistKm))
                .returnDistanceKm(round(returnDistKm))
                .baseFare(BASE_FARE)
                .distanceFare(round(distanceFare))
                .parkingFare(round(parkingFare))
                .surgeFare(isSurge ? round(total - raw) : 0.0)
                .totalFare(round(total))
                .hoursParked(hoursParked)
                .isSurge(isSurge)
                .surgeMultiplier(surgeMultiplier)
                .paymentStatus(ValetFare.PaymentStatus.PENDING)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Surge check — is it currently peak hour?
    // ─────────────────────────────────────────────────────────────────────

    private boolean isSurgeTime(LocalDateTime dateTime) {
        LocalTime now = dateTime.toLocalTime();
        boolean morning = now.isAfter(MORNING_SURGE_START) && now.isBefore(MORNING_SURGE_END);
        boolean evening = now.isAfter(EVENING_SURGE_START) && now.isBefore(EVENING_SURGE_END);
        return morning || evening;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}