package com.smartparking.controller;

import com.smartparking.dtos.response.ValetEarningsResponseDTO;
import com.smartparking.service.ValetEarningsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/valet/earnings")

public class ValetEarningsController {

    private final ValetEarningsService earningsService;
    public ValetEarningsController(ValetEarningsService earningsService) {
        this.earningsService = earningsService;
    }

    // Valet sees their earnings dashboard
    // GET /api/valet/earnings/{valetId}
    @GetMapping("/{valetId}")
    public ResponseEntity<ValetEarningsResponseDTO> getDashboard(@PathVariable Long valetId) {
        return ResponseEntity.ok(earningsService.getEarningsDashboard(valetId));
    }

    // Admin marks all unpaid earnings as paid for a valet
    // POST /api/valet/earnings/{valetId}/payout
    @PostMapping("/{valetId}/payout")
    public ResponseEntity<String> markAsPaid(@PathVariable Long valetId) {
        earningsService.markAsPaid(valetId);
        return ResponseEntity.ok("Payout marked as complete for valet " + valetId);
    }
}