package com.smartparking.service;

import com.smartparking.dtos.DashboardOverviewDTO;
import com.smartparking.dtos.FinancialOverviewDTO;
import com.smartparking.dtos.response.PlatformDashboardResponseDTO;

public interface AnalyticsService {
    DashboardOverviewDTO getDashboardOverview(Long lotId);
    FinancialOverviewDTO getFinancialOverview(Long lotId);
    PlatformDashboardResponseDTO getPlatformDashboard();
}