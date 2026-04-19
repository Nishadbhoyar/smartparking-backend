package com.smartparking.entities.rental;

import com.smartparking.entities.nums.CarRentalStatus;
import com.smartparking.entities.users.Customer;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data                              // ← was missing — no getters/setters without this
@Table(name = "car_rental_bookings")
public class CarRentalBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)  // ← nullable was wrongly true
    private String bookingCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_car_id", nullable = false)
    private RentalCar rentalCar;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double totalAmount;
    private Double depositAmount;

    @Enumerated(EnumType.STRING)
    private CarRentalStatus status = CarRentalStatus.PENDING;  // ← was missing

    private String pickupOtp;
    private String returnOtp;

    private LocalDateTime createdAt;     // ← the field the repository crash needs
    private LocalDateTime completedAt;

    private LocalDateTime actualPickupTime;   // set when pickupOtp verified
    private LocalDateTime actualReturnTime;   // set when returnOtp verified
    private Boolean overdueAlerted = false;   // true once owner notified of overdue

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}