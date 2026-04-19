package com.smartparking.service;

import com.smartparking.dtos.response.ValetEarningsResponseDTO;

public interface ValetEarningsService {
    // Record earning when a valet job is completed
    void recordEarning(Long valetId, Long valetRequestId, double totalFare);

    // Valet sees their earnings dashboard
    ValetEarningsResponseDTO getEarningsDashboard(Long valetId);

    // Admin marks earnings as paid (payout done)
    void markAsPaid(Long valetId);
}