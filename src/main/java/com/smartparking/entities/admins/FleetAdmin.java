package com.smartparking.entities.admins;

import com.smartparking.entities.rental.RentalCompany;
import com.smartparking.entities.users.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "fleet_admins")
public class FleetAdmin extends User {

    private String businessPhone;
    private boolean isVerified = false;

    @OneToOne(mappedBy = "fleetAdmin", cascade = CascadeType.ALL)
    private RentalCompany company;        // one FleetAdmin runs one company
}