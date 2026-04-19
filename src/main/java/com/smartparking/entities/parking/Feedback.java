package com.smartparking.entities.parking;

import com.smartparking.entities.users.Customer;
import com.smartparking.entities.valet.Valet;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
// FIX #10: @Data causes infinite recursion via customer -> feedbacks -> customer
// and parkingLot -> feedbacks -> parkingLot during serialization / logging.
@ToString(of = {"id", "rating"})
@EqualsAndHashCode(of = "id")
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int rating;

    @Column(length = 500)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id")
    private ParkingLot parkingLot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valet_id")
    private Valet valet;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}