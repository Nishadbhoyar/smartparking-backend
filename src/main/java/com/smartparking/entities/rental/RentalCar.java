package com.smartparking.entities.rental;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartparking.entities.admins.CarOwner;
import com.smartparking.entities.nums.FuelType;
import com.smartparking.entities.nums.RentalCarStatus;
import com.smartparking.entities.nums.TransmissionType;
import com.smartparking.entities.nums.VehicleType;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "rental_cars")
public class RentalCar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String make;         // e.g. "Maruti"
    private String model;        // e.g. "Swift"
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType = VehicleType.CAR;  // CAR, BIKE, SCOOTER, SUV, AUTO, VAN, TRUCK

    @Column(nullable = false, unique = true)
    private String licensePlate;

    private String color;
    private Integer seatingCapacity;

    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    private TransmissionType transmission;

    @Enumerated(EnumType.STRING)
    private RentalCarStatus status = RentalCarStatus.AVAILABLE;

    private Double dailyRate;       // ₹ per day
    private Double hourlyRate;      // ₹ per hour (optional)
    private Double securityDeposit; // refundable on return

    // Location where customer picks up the car
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String pickupAddress;

    // Who owns this car — either a CarOwner OR a RentalCompany, never both
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_owner_id")
    private CarOwner carOwner;         // null if it belongs to a company fleet

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_company_id")
    private RentalCompany rentalCompany; // null if it belongs to an individual

    private LocalDateTime listedAt;

    @PrePersist
    public void prePersist() {
        this.listedAt = LocalDateTime.now();
    }
}