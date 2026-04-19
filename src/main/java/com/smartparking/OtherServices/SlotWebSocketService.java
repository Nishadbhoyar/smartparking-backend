package com.smartparking.OtherServices;

import com.smartparking.entities.parking.Slot;
import com.smartparking.websocket.SlotStatusMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SlotWebSocketService {

    // This is the actual Spring Boot "Radio Antenna"
    private final SimpMessagingTemplate messagingTemplate;

    public SlotWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastSlotUpdate(Slot slot) {
        // 1. Pack the data into our message
        SlotStatusMessage message = new SlotStatusMessage(
                slot.getId(),
                slot.getSlotNumber(),
                slot.getParkingLot().getId(),
                slot.getStatus(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        );

        // 2. Broadcast it to the specific parking lot's "channel"
        // Frontend React app will listen to this exact string!
        messagingTemplate.convertAndSend(
                "/topic/parking-lot/" + slot.getParkingLot().getId() + "/slots",
                message
        );
    }
}