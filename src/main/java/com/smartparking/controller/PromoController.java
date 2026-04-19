package com.smartparking.controller;

import com.smartparking.service.PromoService;
import com.smartparking.dtos.request.ApplyPromoRequestDTO;
import com.smartparking.dtos.request.CreatePromoRequestDTO;
import com.smartparking.dtos.response.PromoResponseDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/promo")

public class PromoController {

    private final PromoService promoService;
    public PromoController(PromoService promoService) { this.promoService = promoService; }

    // Admin creates promo code
    @PostMapping("/create")
    public ResponseEntity<PromoResponseDTO> createPromo(@RequestBody CreatePromoRequestDTO dto) {
        return new ResponseEntity<>(promoService.createPromo(dto), HttpStatus.CREATED);
    }

    // Customer checks if code is valid before applying
    // GET /api/promo/validate?code=FIRST50&amount=300
    // GET /api/promo/validate?code=FIRST50&amount=300&customerId=42  (M-02: per-customer check)
    @GetMapping("/validate")
    public ResponseEntity<PromoResponseDTO> validate(
            @RequestParam String code,
            @RequestParam Double amount,
            @RequestParam(required = false) Long customerId) {
        return ResponseEntity.ok(promoService.validatePromo(code, amount, customerId));
    }

    // Customer applies code at checkout
    @PostMapping("/apply")
    public ResponseEntity<PromoResponseDTO> applyPromo(@RequestBody ApplyPromoRequestDTO dto) {
        return ResponseEntity.ok(promoService.applyPromo(dto));
    }

    // Admin sees all promo codes
    @GetMapping("/all")
    public ResponseEntity<List<PromoResponseDTO>> getAllPromos() {
        return ResponseEntity.ok(promoService.getAllPromos());
    }

    // Admin deactivates a promo code
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<PromoResponseDTO> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(promoService.deactivatePromo(id));
    }
}