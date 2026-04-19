package com.smartparking.dtos.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeedbackResponseDTO {
    private Long id;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    // We send the name back so the frontend can say "Reviewed by John Doe"
    private String customerName;

    // M-05 FIX: Include entity IDs so callers can identify what was reviewed
    private Long parkingLotId;   // null when this is a valet review
    private Long valetId;        // null when this is a lot review
}