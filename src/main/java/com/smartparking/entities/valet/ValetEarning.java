package com.smartparking.entities.valet;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "valet_earnings")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ValetEarning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "valet_id", nullable = false)
    private Valet valet;

    private Long   valetRequestId;     // which job this earning came from
    private Double jobAmount;          // total fare for this job
    private Double valetCut;           // valet's share (e.g. 70%)
    private Double platformCut;        // platform's share (e.g. 30%)
    private boolean paid;              // has platform paid out to valet?

    private LocalDateTime earnedAt;
    private LocalDateTime paidAt;

    @PrePersist
    public void prePersist() {
        this.earnedAt = LocalDateTime.now();
        this.paid = false;
    }
}