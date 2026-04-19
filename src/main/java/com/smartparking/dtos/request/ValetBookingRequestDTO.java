package com.smartparking.dtos.request;

import lombok.Data;

@Data
public class ValetBookingRequestDTO {
    private Long customerId;
    private String mobileNo;
    private String carPlateNo;
    private double pickupLatitude;
    private double pickupLongitude;
}