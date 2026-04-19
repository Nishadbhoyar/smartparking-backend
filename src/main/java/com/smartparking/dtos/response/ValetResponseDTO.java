package com.smartparking.dtos.response;

import com.smartparking.entities.nums.ValetStatus;
import lombok.Data;
import java.util.List;

@Data
public class ValetResponseDTO {
    private Long id;
    private String customerName;
    private String carPlateNo;
    private String valetName;
    private ValetStatus status;

    // Customer sees their OTPs
    private String pickupOtp;
    private String dropoffOtp;

    // Filled once parked
    private String parkingLotName;
    private String slotNumber;
    private List<String> carImages;

    // NEW: parked car coordinates — customer uses these to see their car on a map
    private Double parkedLatitude;
    private Double parkedLongitude;
}