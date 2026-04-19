package com.smartparking.entities.rental;


import com.smartparking.entities.admins.FleetAdmin;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "rental_companies")
public class RentalCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String companyName;

    private String registrationNumber;
    private String address;
    private String city;
    private String contactEmail;
    private String contactPhone;
    private boolean platformVerified = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fleet_admin_id", nullable = false)
    private FleetAdmin fleetAdmin;

    @OneToMany(mappedBy = "rentalCompany", cascade = CascadeType.ALL)
    private List<RentalCar> fleet;
}