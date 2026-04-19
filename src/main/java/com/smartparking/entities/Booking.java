package com.smartparking.entities;

import com.smartparking.entities.nums.BookingStatus;
import com.smartparking.entities.parking.ParkingLot;
import com.smartparking.entities.parking.Slot;
import com.smartparking.entities.users.Customer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
// FIX #10: @Data auto-generates toString() that traverses ALL fields including lazy collections
// and bidirectional links — causes infinite recursion or LazyInitializationException during
// serialization / logging. Replace with explicit @ToString and @EqualsAndHashCode.
@ToString(of = {"id", "bookingCode", "status"})
@EqualsAndHashCode(of = "id")
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String bookingCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;

    private String dropoffOtp;
    private String pickupOtp;
}