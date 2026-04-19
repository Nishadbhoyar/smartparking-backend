package com.smartparking.entities.parking;

import com.smartparking.entities.admins.ParkingLotAdmin;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Entity
@Getter
@Setter
// FIX #10: @Data on ParkingLot causes infinite recursion via slots -> slot.parkingLot -> slots...
// and triggers LazyInitializationException in toString() if session is closed.
@ToString(of = {"id", "name", "status"})
@EqualsAndHashCode(of = "id")
@Table(name = "parking_lots")
public class ParkingLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Double latitude;
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_admin_id", nullable = false)
    private ParkingLotAdmin parkingLotAdmin;

    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Slot> slots;

    private boolean isCompanyVerified = false;

    @Enumerated(EnumType.STRING)
    private com.smartparking.entities.nums.ParkingLotStatus status =
            com.smartparking.entities.nums.ParkingLotStatus.ACTIVE;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "parking_lot_features",
            joinColumns = @JoinColumn(name = "parking_lot_id"),
            inverseJoinColumns = @JoinColumn(name = "feature_id")
    )
    private java.util.Set<Feature> features = new java.util.HashSet<>();
}