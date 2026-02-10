package com.smartparking.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double totalAmount;

    // Identifies the journey: "SELF", "NEARBY", or "VALET"
    private String serviceType;

    // --- LOCATION DATA ---

    // Where the valet picks up the car from the user
    private Double pickupLat;
    private Double pickupLng;

    // Where the car is actually parked (for Valet proof)
    private Double parkedLat;
    private Double parkedLng;

    // The photo proof uploaded by the valet
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String parkedProofImage;

    private String status; // PENDING, VALET_REQUESTED, VALET_PICKED_UP, PARKED, COMPLETED

    // Vehicle details
    private String vehicleNumber;
    private String vehicleModel;
    private String vehicleType; // CAR, BIKE, HEAVY, CYCLE
    private String contactNumber;
    private String driverName;

    // --- RELATIONSHIPS ---

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "lot_id", nullable = true) // Nullable for valet pickup until parked
    private ParkingLot lot;

    @ManyToOne
    @JoinColumn(name = "valet_id")
    private User valet;

    // --- CONSTRUCTORS ---
    public Booking() {
    }

    // --- GETTERS AND SETTERS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ParkingLot getLot() {
        return lot;
    }

    public void setLot(ParkingLot lot) {
        this.lot = lot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // --- LOCATION GETTERS/SETTERS ---

    public Double getPickupLat() {
        return pickupLat;
    }

    public void setPickupLat(Double pickupLat) {
        this.pickupLat = pickupLat;
    }

    public Double getPickupLng() {
        return pickupLng;
    }

    public void setPickupLng(Double pickupLng) {
        this.pickupLng = pickupLng;
    }

    public Double getParkedLat() {
        return parkedLat;
    }

    public void setParkedLat(Double parkedLat) {
        this.parkedLat = parkedLat;
    }

    public Double getParkedLng() {
        return parkedLng;
    }

    public void setParkedLng(Double parkedLng) {
        this.parkedLng = parkedLng;
    }

    public String getParkedProofImage() {
        return parkedProofImage;
    }

    public void setParkedProofImage(String parkedProofImage) {
        this.parkedProofImage = parkedProofImage;
    }

    // --- VEHICLE GETTERS/SETTERS ---

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    // --- VALET GETTER/SETTER ---

    public User getValet() {
        return valet;
    }

    public void setValet(User valet) {
        this.valet = valet;
    }
}