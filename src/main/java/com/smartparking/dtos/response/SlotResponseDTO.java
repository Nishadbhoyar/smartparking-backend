package com.smartparking.dtos.response;

import com.smartparking.entities.nums.SlotStatus;
import com.smartparking.entities.nums.SlotType;
import lombok.Data;

@Data
public class SlotResponseDTO {
    private Long   id;

    // Original field
    private String slotNumber;

    // "name" alias — frontend uses slot.name
    private String name;

    private String zone;
    private String floor;

    private SlotStatus status;

    // Original field
    private SlotType slotType;

    // "type" alias — frontend uses slot.type
    private SlotType type;

    private Long   parkingLotId;
    private double hourlyRate;
}
