package com.smartparking.entities.users;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "customers")
public class Customer extends User {

    // Customer specific features
    private String defaultLicensePlate;

    // You could link them to their Booking history here later:
    // @OneToMany(mappedBy = "customer")
    // private List<Booking> myBookings;
}
