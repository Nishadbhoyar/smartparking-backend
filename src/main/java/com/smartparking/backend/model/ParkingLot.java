package com.smartparking.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parking_lots")
public class ParkingLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String description;
    private String type;
    // 👇 ADD THIS FIELD (Default to "ACTIVE")
    private String status = "ACTIVE";
    private Double latitude;
    private Double longitude;

    // Amenities (Flattened)
    private boolean cctv;
    private boolean security;
    private boolean covered;
    private boolean evCharging;

    // 👇 ADDED THIS FIELD: To link the lot to the Admin/Owner
    @Column(name = "owner_id")
    private Long ownerId;

    // The One-to-Many Relationship
    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParkingSlot> slots = new ArrayList<>();

    // Helper to add slots
    public void addSlot(ParkingSlot slot) {
        slots.add(slot);
        slot.setParkingLot(this);
    }

    // --- CONSTRUCTORS ---
    public ParkingLot() {
    }

    // --- GETTERS AND SETTERS ---

    // 👇 ADDED GETTER/SETTER FOR OWNER ID
    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
    // 👆 END ADDITION

    // 👇 ADD GETTER AND SETTER
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean isCctv() {
        return cctv;
    }

    public void setCctv(boolean cctv) {
        this.cctv = cctv;
    }

    public boolean isSecurity() {
        return security;
    }

    public void setSecurity(boolean security) {
        this.security = security;
    }

    public boolean isCovered() {
        return covered;
    }

    public void setCovered(boolean covered) {
        this.covered = covered;
    }

    public boolean isEvCharging() {
        return evCharging;
    }

    public void setEvCharging(boolean evCharging) {
        this.evCharging = evCharging;
    }

    public List<ParkingSlot> getSlots() {
        return slots;
    }

    public void setSlots(List<ParkingSlot> slots) {
        this.slots = slots;
    }
}