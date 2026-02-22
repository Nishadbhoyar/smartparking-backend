package com.smartparking.backend.controller;

import com.smartparking.backend.dto.ParkingLotRequest;
import com.smartparking.backend.model.ParkingLot;
import com.smartparking.backend.model.ParkingSlot;
import com.smartparking.backend.repository.ParkingLotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull; // 1. Added Import
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/parking-lots")
@CrossOrigin(origins = "http://localhost:5173")
@SuppressWarnings("null") // 2. Added this to silence the warnings
public class ParkingLotController {

    @Autowired
    private ParkingLotRepository parkingLotRepository;

    // ==========================================
    // 1. GET ALL LOTS (Handles Map & Admin Dashboard)
    // ==========================================
    @GetMapping
    public ResponseEntity<List<ParkingLot>> getAllParkingLots(@RequestParam(required = false) Long ownerId) {
        try {
            if (ownerId != null) {
                // If ownerId is provided, fetch ONLY their lots (Manage Mode)
                return ResponseEntity.ok(parkingLotRepository.findByOwnerId(ownerId));
            } else {
                // If no ID, fetch ALL (User Map Mode)
                return ResponseEntity.ok(parkingLotRepository.findAll());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==========================================
    // 2. CREATE PARKING LOT
    // ==========================================
    @PostMapping
    public ResponseEntity<?> addParkingLot(@RequestBody ParkingLotRequest request,
            @RequestParam @NonNull Long ownerId) {
        try {
            // 1. Create Parent
            ParkingLot lot = new ParkingLot();
            lot.setName(request.getName());
            lot.setAddress(request.getAddress());
            lot.setDescription(request.getDescription());

            // Set Parking Type
            lot.setType(request.getType());

            // LINK TO ADMIN
            lot.setOwnerId(ownerId);

            // 2. Set Location
            if (request.getLocation() != null) {
                lot.setLatitude(request.getLocation().getLatitude());
                lot.setLongitude(request.getLocation().getLongitude());
            }

            // 3. Set Amenities
            if (request.getAmenities() != null) {
                lot.setCctv(request.getAmenities().isCctv());
                lot.setSecurity(request.getAmenities().isSecurity());
                lot.setCovered(request.getAmenities().isCovered());
                lot.setEvCharging(request.getAmenities().isEvCharging());
            }

            // 4. Set Slots
            if (request.getParkingSlots() != null) {
                for (ParkingLotRequest.SlotConfig slotDTO : request.getParkingSlots()) {
                    ParkingSlot slot = new ParkingSlot();
                    slot.setVehicleType(slotDTO.getVehicleType());
                    slot.setCapacity(slotDTO.getCapacity());
                    slot.setPrice(slotDTO.getPrice());

                    // Link them
                    lot.addSlot(slot);
                }
            }

            ParkingLot savedLot = parkingLotRepository.save(lot);
            return ResponseEntity.ok(savedLot);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error creating parking lot: " + e.getMessage());
        }
    }

    // ==========================================
    // 3. GET SINGLE PARKING LOT BY ID
    // ==========================================
    @GetMapping("/{id}")
    public ResponseEntity<?> getParkingLotById(@PathVariable @NonNull Long id) {
        return parkingLotRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // 4. UPDATE PARKING LOT
    // ==========================================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateParkingLot(@PathVariable @NonNull Long id, @RequestBody ParkingLotRequest request) {
        return parkingLotRepository.findById(id).map(lot -> {
            // Update basic fields
            if (request.getName() != null)
                lot.setName(request.getName());
            if (request.getAddress() != null)
                lot.setAddress(request.getAddress());
            if (request.getDescription() != null)
                lot.setDescription(request.getDescription());

            // Update Parking Type
            if (request.getType() != null)
                lot.setType(request.getType());

            // Update location
            if (request.getLocation() != null) {
                if (request.getLocation().getLatitude() != null) {
                    lot.setLatitude(request.getLocation().getLatitude());
                }
                if (request.getLocation().getLongitude() != null) {
                    lot.setLongitude(request.getLocation().getLongitude());
                }
            }

            // Update amenities
            if (request.getAmenities() != null) {
                lot.setCctv(request.getAmenities().isCctv());
                lot.setSecurity(request.getAmenities().isSecurity());
                lot.setCovered(request.getAmenities().isCovered());
                lot.setEvCharging(request.getAmenities().isEvCharging());
            }

            ParkingLot updated = parkingLotRepository.save(lot);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // 5. DELETE PARKING LOT
    // ==========================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteParkingLot(@PathVariable Long id) {
        try {
            parkingLotRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Parking lot deleted successfully"));

        } catch (DataIntegrityViolationException e) {
            // This catches the foreign key error!
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error",
                            "Cannot delete this parking lot because it has existing bookings or slots tied to it."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete parking lot: " + e.getMessage()));
        }
    }

    // ==========================================
    // 6. TOGGLE STATUS (ACTIVE / PAUSED)
    // ==========================================
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateParkingLotStatus(@PathVariable @NonNull Long id,
            @RequestBody java.util.Map<String, String> statusUpdate) {
        return parkingLotRepository.findById(id).map(lot -> {
            String newStatus = statusUpdate.get("status");
            if (newStatus != null) {
                lot.setStatus(newStatus);
                parkingLotRepository.save(lot);
                return ResponseEntity.ok().body("Status updated to " + newStatus);
            }
            return ResponseEntity.badRequest().body("Status is required");
        }).orElse(ResponseEntity.notFound().build());
    }
}