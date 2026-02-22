package com.smartparking.backend.controller;

import com.smartparking.backend.model.Booking;
import com.smartparking.backend.repository.BookingRepository;
import com.smartparking.backend.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class ValetCompletionController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private BookingRepository bookingRepository;

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeBooking(
            @PathVariable Long id,
            @RequestParam("parkedLat") Double parkedLat,
            @RequestParam("parkedLng") Double parkedLng,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        try {
            Optional<Booking> optionalBooking = bookingRepository.findById(id);
            if (optionalBooking.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Booking not found"));
            }

            Booking booking = optionalBooking.get();

            // Save Coordinates
            booking.setParkedLat(parkedLat);
            booking.setParkedLng(parkedLng);

            // Upload to Cloudinary and save URL
            if (files != null && files.length > 0) {
                String cloudUrl = cloudinaryService.uploadImage(files[0]);
                booking.setParkedProofImage(cloudUrl);
            }

            // Update Status
            booking.setStatus("PARKED");
            bookingRepository.save(booking);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Vehicle parked successfully!");
            response.put("parkedProofImage", booking.getParkedProofImage());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to complete task: " + e.getMessage()));
        }
    }
}