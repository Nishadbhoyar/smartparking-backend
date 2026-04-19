package com.smartparking.dtos.response;

import com.smartparking.entities.nums.ParkingLotStatus;
import lombok.Data;

import java.util.List;

@Data
public class ParkingLotResponseDTO {
    private Long id;
    private String name;
    private double latitude;
    private double longitude;

    // Safe, flat data about the owner
    private Long adminId;
    private String adminName;

    // --- NEW: Add these so Postman and React can see them! ---
    private boolean companyVerified;
    private ParkingLotStatus status;
    private List<String> features;
}