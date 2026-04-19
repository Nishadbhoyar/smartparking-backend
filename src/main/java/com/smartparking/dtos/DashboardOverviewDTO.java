package com.smartparking.dtos;

import lombok.Data;

@Data
public class DashboardOverviewDTO {
    private long totalSlots;
    private long occupiedSlots;
    private long availableSlots;
    private long maintenanceSlots;
    private Double todaysEarnings;
}