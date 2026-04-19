package com.smartparking.dtos.request;
import lombok.Data;

@Data
public class ApplyPromoRequestDTO {
    private String code;
    private Long   customerId;
    private Double bookingAmount;  // original booking total before discount
}