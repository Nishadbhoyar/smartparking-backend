package com.smartparking.controller;

import com.smartparking.dtos.response.PlatformDashboardResponseDTO;
import com.smartparking.entities.admins.CarOwner;
import com.smartparking.entities.admins.FleetAdmin;
import com.smartparking.entities.admins.ParkingLotAdmin;
import com.smartparking.entities.users.User;
import com.smartparking.repositories.*;
import com.smartparking.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/super-admin")

public class SuperAdminController {

    @Autowired private ParkingLotAdminRepository parkingLotAdminRepository;
    @Autowired private CarOwnerRepository         carOwnerRepository;
    @Autowired private FleetAdminRepository        fleetAdminRepository;
    @Autowired private RentalCompanyRepository     rentalCompanyRepository;
    @Autowired private UserRepository              userRepository;
    @Autowired private AnalyticsService            analyticsService;

    // ── Platform dashboard ──────────────────────────────────────────────────
    // GET /api/super-admin/platform-stats
    @GetMapping("/platform-stats")
    public ResponseEntity<PlatformDashboardResponseDTO> getPlatformStats() {
        return ResponseEntity.ok(analyticsService.getPlatformDashboard());
    }

    // ── All users ───────────────────────────────────────────────────────────
    // GET /api/super-admin/all-users
    @GetMapping("/all-users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // ── Parking lot admins ──────────────────────────────────────────────────
    @GetMapping("/parking-lot-admins")
    public ResponseEntity<List<ParkingLotAdmin>> getAllParkingLotAdmins() {
        return ResponseEntity.ok(parkingLotAdminRepository.findAll());
    }

    // ── Car owners ──────────────────────────────────────────────────────────
    @GetMapping("/car-owners")
    public ResponseEntity<List<CarOwner>> getAllCarOwners() {
        return ResponseEntity.ok(carOwnerRepository.findAll());
    }

    @PutMapping("/car-owners/{id}/verify")
    public ResponseEntity<CarOwner> verifyCarOwner(@PathVariable Long id) {
        CarOwner owner = carOwnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car owner not found"));
        owner.setVerified(true);
        return ResponseEntity.ok(carOwnerRepository.save(owner));
    }

    // ── Fleet admins ────────────────────────────────────────────────────────
    @GetMapping("/fleet-admins")
    public ResponseEntity<List<FleetAdmin>> getAllFleetAdmins() {
        return ResponseEntity.ok(fleetAdminRepository.findAll());
    }

    @PutMapping("/fleet-admins/{id}/verify")
    public ResponseEntity<FleetAdmin> verifyFleetAdmin(@PathVariable Long id) {
        FleetAdmin admin = fleetAdminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fleet admin not found"));
        admin.setVerified(true);
        rentalCompanyRepository.findByFleetAdminId(id).ifPresent(company -> {
            company.setPlatformVerified(true);
            rentalCompanyRepository.save(company);
        });
        return ResponseEntity.ok(fleetAdminRepository.save(admin));
    }
}