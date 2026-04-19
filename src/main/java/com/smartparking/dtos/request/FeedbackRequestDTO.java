package com.smartparking.dtos.request;

import lombok.Data;

@Data
public class FeedbackRequestDTO {
    private Long customerId;
    private int rating;
    private String comment;

    // The frontend can send one, the other, or both depending on the flow!
    private Long parkingLotId;
    private Long valetId;
}