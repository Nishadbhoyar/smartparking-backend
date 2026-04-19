package com.smartparking.entities.admins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartparking.entities.rental.RentalCar;
import com.smartparking.entities.users.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "car_owners")
public class CarOwner extends User {

    private String phone;
    private String aadhaarNumber;       // for KYC verification
    private boolean isVerified = false; // platform must verify before they can list

    @JsonIgnore
    @OneToMany(mappedBy = "carOwner", cascade = CascadeType.ALL)
    private List<RentalCar> listedCars;
}