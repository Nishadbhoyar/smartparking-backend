package com.smartparking.dtos.response;

import com.smartparking.entities.nums.BookingStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingResponseDTO {
    private Long id;
    private String bookingCode;
    private String customerName;
    private Long   parkingLotId;
    private String parkingLotName;
    private String slotNumber;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Double totalAmount;
    private BookingStatus status;
}