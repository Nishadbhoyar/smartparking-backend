package com.smartparking.dtos.request;

import lombok.Data;

@Data
public class ParkingLotRequestDTO {
    private String name;
    private double latitude;
    private double longitude;

    // We need to know which Admin is claiming ownership of this new lot
    private Long adminId;
}