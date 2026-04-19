package com.smartparking.controller;

import com.smartparking.dtos.request.ParkingLotRequestDTO;
import com.smartparking.dtos.response.ParkingLotResponseDTO;
import com.smartparking.service.ParkingLotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-lots")

public class ParkingLotController {

    @Autowired
    private ParkingLotService parkingLotService;

    @PostMapping("/add")
    public ResponseEntity<ParkingLotResponseDTO> addParkingLot(@RequestBody ParkingLotRequestDTO requestDTO) {
        ParkingLotResponseDTO savedLot = parkingLotService.createParkingLot(requestDTO);
        return new ResponseEntity<>(savedLot, HttpStatus.CREATED);
    }

    // GET: /api/parking-lots/admin/1
    @GetMapping("/admin/{adminId}")
    public ResponseEntity<List<ParkingLotResponseDTO>> getLotsByAdmin(@PathVariable Long adminId) {
        return new ResponseEntity<>(parkingLotService.getLotsByAdmin(adminId), HttpStatus.OK);
    }

    @PutMapping("/{lotId}/admin/{adminId}")
    public ResponseEntity<ParkingLotResponseDTO> updateParkingLot(
            @PathVariable Long lotId,
            @PathVariable Long adminId,
            @RequestBody ParkingLotRequestDTO requestDTO) {

        ParkingLotResponseDTO updatedLot = parkingLotService.updateParkingLot(lotId, adminId, requestDTO);
        return new ResponseEntity<>(updatedLot, HttpStatus.OK);
    }

    @DeleteMapping("/{lotId}/admin/{adminId}")
    public ResponseEntity<Void> deleteParkingLot(
            @PathVariable Long lotId,
            @PathVariable Long adminId) {

        parkingLotService.deleteParkingLot(lotId, adminId);
        // Returns a 204 No Content status, which is the standard for a successful delete!
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // PUT: /api/parking-lots/1/status?status=CLOSED&adminId=2
    // H-06 FIX: adminId required — ownership enforced in service layer
    @PutMapping("/{lotId}/status")
    public ResponseEntity<ParkingLotResponseDTO> updateLotStatus(
            @PathVariable Long lotId,
            @RequestParam Long adminId,
            @RequestParam com.smartparking.entities.nums.ParkingLotStatus status) {
        return new ResponseEntity<>(parkingLotService.updateLotStatus(lotId, adminId, status), HttpStatus.OK);
    }

    // POST: /api/parking-lots/1/features/1?adminId=2
    // H-06 FIX: adminId required — ownership enforced in service layer
    @PostMapping("/{lotId}/features/{featureId}")
    public ResponseEntity<ParkingLotResponseDTO> addFeatureToLot(
            @PathVariable Long lotId,
            @PathVariable Long featureId,
            @RequestParam Long adminId) {
        return new ResponseEntity<>(parkingLotService.addFeatureToLot(lotId, adminId, featureId), HttpStatus.OK);
    }

    // GET: /api/parking-lots/1
    @GetMapping("/{lotId}")
    public ResponseEntity<ParkingLotResponseDTO> getLotById(@PathVariable Long lotId) {
        return new ResponseEntity<>(parkingLotService.getLotById(lotId), HttpStatus.OK);
    }

    // GET: /api/parking-lots/nearby?lat=19.076&lng=72.8777&limit=5
    @GetMapping("/nearby")
    public ResponseEntity<List<ParkingLotResponseDTO>> getNearbyLots(
            @RequestParam("lat") double latitude,
            @RequestParam("lng") double longitude,
            @RequestParam(defaultValue = "5") int limit) {

        List<ParkingLotResponseDTO> nearbyLots = parkingLotService.getNearbyLots(latitude, longitude, limit);
        return new ResponseEntity<>(nearbyLots, HttpStatus.OK);
    }
}