package com.smartparking.entities.parking;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "features")
public class Feature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    // Example names: "CCTV", "24/7 Security", "Valet Desk", "Wheelchair Accessible"
}