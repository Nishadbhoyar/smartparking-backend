package com.smartparking.dtos;

import lombok.Data;

@Data
public class FinancialOverviewDTO {
    private Double totalRevenue;
    private long totalTransactions;
    private Double averageTransactionValue;
}