package com.smartparking.backend.dto;

import java.util.List;

public class ParkingLotRequest {

    private String name;
    private String address;
    private String description;
    private String type;

    // Nested Objects
    private LocationData location;
    private AmenitiesData amenities;
    private List<SlotConfig> parkingSlots;

    // --- GETTERS AND SETTERS ---

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public LocationData getLocation() {
        return location;
    }

    public void setLocation(LocationData location) {
        this.location = location;
    }

    public AmenitiesData getAmenities() {
        return amenities;
    }

    public void setAmenities(AmenitiesData amenities) {
        this.amenities = amenities;
    }

    public List<SlotConfig> getParkingSlots() {
        return parkingSlots;
    }

    public void setParkingSlots(List<SlotConfig> parkingSlots) {
        this.parkingSlots = parkingSlots;
    }

    // --- INNER STATIC CLASSES (To match JSON structure) ---

    public static class LocationData {
        private Double latitude;
        private Double longitude;

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
    }

    public static class AmenitiesData {
        private boolean cctv;
        private boolean security;
        private boolean covered;
        private boolean evCharging;

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
    }

    public static class SlotConfig {
        private String vehicleType;
        private int capacity;
        private double price;

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
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }
}