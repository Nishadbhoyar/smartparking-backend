package com.smartparking.dtos.request;
import com.smartparking.entities.nums.Role;
import lombok.Data;

@Data
public class UserRegistrationDTO {
    private String name;
    private String email;
    private String password;
    private Role role;

    // CUSTOMER
    private String defaultLicensePlate;

    // VALET
    private String drivingLicenseNumber;

    // CAR_OWNER
    private String aadhaarNumber;

    // FLEET_ADMIN
    private String businessPhone;

    // PARKING_LOT_ADMIN
    private String businessRegistrationNumber;
}