package com.smartparking.entities.valet;

import com.smartparking.entities.users.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "valets")
public class Valet extends User {

    // Valet specific features
    private boolean isAvailableNow;

    private Double currentLatitude;

    private Double currentLongitude;

    private String drivingLicenseNumber;
}
