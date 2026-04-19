package com.smartparking.controller;

import com.smartparking.dtos.request.ValetBookingRequestDTO;
import com.smartparking.dtos.response.ValetResponseDTO;
import com.smartparking.service.ValetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/valet")
public class ValetController {

    @Autowired
    private ValetService valetService;

    @PostMapping("/request")
    public ResponseEntity<ValetResponseDTO> requestValet(
            @RequestBody ValetBookingRequestDTO requestDTO) {
        return new ResponseEntity<>(valetService.requestValet(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/jobs/available")
    public ResponseEntity<List<ValetResponseDTO>> getAvailableJobs() {
        return new ResponseEntity<>(valetService.getAvailableJobs(), HttpStatus.OK);
    }

    @GetMapping("/jobs/active")
    public ResponseEntity<ValetResponseDTO> getActiveJob(@RequestParam Long valetId) {
        ValetResponseDTO job = valetService.getActiveJob(valetId);
        return job != null ? ResponseEntity.ok(job) : ResponseEntity.noContent().build();
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<ValetResponseDTO> acceptJob(
            @PathVariable Long requestId,
            @RequestParam Long valetId) {
        return new ResponseEntity<>(valetService.acceptJob(requestId, valetId), HttpStatus.OK);
    }

    @PostMapping("/{requestId}/verify-pickup")
    public ResponseEntity<ValetResponseDTO> verifyPickup(
            @PathVariable Long requestId,
            @RequestParam String otp) {
        return new ResponseEntity<>(valetService.verifyPickup(requestId, otp), HttpStatus.OK);
    }

    @PostMapping(value = "/{requestId}/park", consumes = "multipart/form-data")
    public ResponseEntity<ValetResponseDTO> parkVehicle(
            @PathVariable Long requestId,
            @RequestParam Long lotId,
            @RequestParam Long slotId,
            @RequestParam(value = "carImages", required = false) List<MultipartFile> carImages) {
        return new ResponseEntity<>(
                valetService.parkVehicle(requestId, lotId, slotId, carImages), HttpStatus.OK);
    }

    @PostMapping("/{requestId}/request-return")
    public ResponseEntity<ValetResponseDTO> requestVehicleBack(
            @PathVariable Long requestId) {
        return new ResponseEntity<>(valetService.requestVehicleBack(requestId), HttpStatus.OK);
    }

    @PostMapping("/{requestId}/verify-dropoff")
    public ResponseEntity<ValetResponseDTO> verifyDropoff(
            @PathVariable Long requestId,
            @RequestParam String otp) {
        return new ResponseEntity<>(valetService.verifyDropoff(requestId, otp), HttpStatus.OK);
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<ValetResponseDTO> getRequestStatus(@PathVariable Long requestId) {
        return ResponseEntity.ok(valetService.getRequestById(requestId));
    }

    /**
     * GET /api/valet/customer/{customerId}/active
     * Returns the active valet request for this customer (REQUESTED → RETURN_REQUESTED).
     * Returns 204 No Content if no active job exists.
     * Used by CustomerDashboard to show live valet status card.
     */
    @GetMapping("/customer/{customerId}/active")
    public ResponseEntity<ValetResponseDTO> getActiveValetForCustomer(
            @PathVariable Long customerId) {
        ValetResponseDTO dto = valetService.getActiveValetForCustomer(customerId);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }
}