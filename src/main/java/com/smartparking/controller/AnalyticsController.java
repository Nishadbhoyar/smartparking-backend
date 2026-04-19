package com.smartparking.controller;


import com.smartparking.dtos.DashboardOverviewDTO;
import com.smartparking.dtos.FinancialOverviewDTO;
import com.smartparking.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    // Powers Dashboard 1 (Overview)
    @GetMapping("/overview/lot/{lotId}")
    public ResponseEntity<DashboardOverviewDTO> getDashboardOverview(@PathVariable Long lotId) {
        return new ResponseEntity<>(analyticsService.getDashboardOverview(lotId), HttpStatus.OK);
    }

    // Powers Dashboard 4 (Earnings & Revenue)
    @GetMapping("/finance/lot/{lotId}")
    public ResponseEntity<FinancialOverviewDTO> getFinancialOverview(@PathVariable Long lotId) {
        return new ResponseEntity<>(analyticsService.getFinancialOverview(lotId), HttpStatus.OK);
    }
}