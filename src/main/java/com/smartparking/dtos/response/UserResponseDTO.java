package com.smartparking.dtos.response;

import com.smartparking.entities.nums.Role;
import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;           // ← capital L, not primitive long
    private String name;
    private String email;
    private Role role;

    private String defaultLicensePlate;
    private String drivingLicenseNumber;
    private String aadhaarNumber;
    private String businessPhone;
    private String businessRegistrationNumber;
}