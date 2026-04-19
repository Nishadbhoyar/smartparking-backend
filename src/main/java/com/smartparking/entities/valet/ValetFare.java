package com.smartparking.entities.valet;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Stores the complete fare breakdown for every valet job.
 * Created when valet picks up vehicle, finalized when job completes.
 */
@Entity
@Table(name = "valet_fares")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValetFare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to the valet request this fare belongs to
    @OneToOne
    @JoinColumn(name = "valet_request_id", nullable = false)
    private ValetRequest valetRequest;

    // ── Distance charges ─────────────────────────────────────────
    private double pickupDistanceKm;       // km from valet to user
    private double parkingDistanceKm;      // km from user to parking lot
    private double returnDistanceKm;       // km from lot back to user

    // ── Fare components ──────────────────────────────────────────
    private double baseFare;               // flat base fee (e.g. ₹50)
    private double distanceFare;           // per-km charge on total distance
    private double parkingFare;            // hourly rate × hours parked
    private double surgeFare;              // surge multiplier during peak hours
    private double totalFare;              // final amount to charge customer

    // ── Parking duration ─────────────────────────────────────────
    private LocalDateTime parkedAt;        // when vehicle was parked
    private LocalDateTime returnedAt;      // when vehicle was returned
    private double hoursParked;            // calculated duration

    // ── Surge info ───────────────────────────────────────────────
    private double surgeMultiplier;        // e.g. 1.0 = normal, 1.5 = surge
    private boolean isSurge;

    // ── Payment ──────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.paymentStatus = PaymentStatus.PENDING;
    }

    public enum PaymentStatus {
        PENDING,    // job not done yet
        CALCULATED, // job done, amount calculated, waiting for payment
        PAID,       // customer paid
        REFUNDED    // refunded due to cancellation
    }
}