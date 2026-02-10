// package com.smartparking.backend.model;

// import com.fasterxml.jackson.annotation.JsonIgnore;
// import jakarta.persistence.*;

// @Entity
// @Table(name = "parking_slots")
// public class ParkingSlot {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     private String vehicleType; // CAR, BIKE, TRUCK
//     private int capacity;
//     private double price;

//     @ManyToOne(fetch = FetchType.LAZY)
//     @JoinColumn(name = "parking_lot_id")
//     @JsonIgnore // Important: Prevents infinite loop
//     private ParkingLot parkingLot;

//     // Getters and Setters
//     public Long getId() { return id; }
//     public void setId(Long id) { this.id = id; }
//     public String getVehicleType() { return vehicleType; }
//     public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
//     public int getCapacity() { return capacity; }
//     public void setCapacity(int capacity) { this.capacity = capacity; }
//     public double getPrice() { return price; }
//     public void setPrice(double price) { this.price = price; }
//     public ParkingLot getParkingLot() { return parkingLot; }
//     public void setParkingLot(ParkingLot parkingLot) { this.parkingLot = parkingLot; }
// }

package com.smartparking.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "parking_slots")
public class ParkingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicleType; // CAR, BIKE, TRUCK, CYCLE, HEAVY
    private int capacity;
    private int availableSlots; // Track available slots
    private double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id")
    @JsonIgnore // Important: Prevents infinite loop
    private ParkingLot parkingLot;

    // --- CONSTRUCTORS ---
    public ParkingSlot() {
    }

    // --- GETTERS AND SETTERS ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
        // Initialize availableSlots to capacity when set
        if (this.availableSlots == 0) {
            this.availableSlots = capacity;
        }
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public void setParkingLot(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
    }

    // --- HELPER METHODS ---
    public boolean isAvailable() {
        return availableSlots > 0;
    }

    public void decrementAvailableSlots() {
        if (availableSlots > 0) {
            availableSlots--;
        }
    }

    public void incrementAvailableSlots() {
        if (availableSlots < capacity) {
            availableSlots++;
        }
    }
}