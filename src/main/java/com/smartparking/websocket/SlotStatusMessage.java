package com.smartparking.websocket;

import com.smartparking.entities.nums.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlotStatusMessage {
    private Long slotId;
    private String slotNumber;
    private Long parkingLotId;
    private SlotStatus status;
    private String updatedAt;
}