package com.smartparking.repositories;

import com.smartparking.entities.featuresentites.PromoUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromoUsageRepository extends JpaRepository<PromoUsage, Long> {
    boolean existsByPromoCodeIdAndCustomerId(Long promoCodeId, Long customerId);
}