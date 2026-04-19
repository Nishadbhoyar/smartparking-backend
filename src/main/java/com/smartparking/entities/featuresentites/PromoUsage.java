package com.smartparking.entities.featuresentites;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_usage",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"promo_code_id", "customer_id"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PromoUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "promo_code_id")
    private PromoCode promoCode;

    private Long customerId;
    private LocalDateTime usedAt;

    @PrePersist
    public void prePersist() { this.usedAt = LocalDateTime.now(); }
}