package com.smartparking.service;

import com.smartparking.dtos.request.ApplyPromoRequestDTO;
import com.smartparking.dtos.request.CreatePromoRequestDTO;
import com.smartparking.dtos.response.PromoResponseDTO;

import java.util.List;

public interface PromoService {

    PromoResponseDTO createPromo(CreatePromoRequestDTO dto);

    PromoResponseDTO applyPromo(ApplyPromoRequestDTO dto);

    // M-02 FIX: customerId is optional (nullable).
    // Pass it to enforce per-customer usage check at validation time.
    // Omit (null) for anonymous pre-checks that don't need a customer context.
    PromoResponseDTO validatePromo(String code, Double bookingAmount, Long customerId);

    List<PromoResponseDTO> getAllPromos();

    PromoResponseDTO deactivatePromo(Long promoId);
}