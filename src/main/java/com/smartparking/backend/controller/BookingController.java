package com.smartparking.backend.controller;

import com.smartparking.backend.model.Booking;
import com.smartparking.backend.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType; // ✅ Import for Multipart
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // ✅ Import for Files

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:5173")
@SuppressWarnings("null")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    // 1. CREATE BOOKING
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        try {
            booking.setStartTime(LocalDateTime.now());
            if (booking.getStatus() == null) {
                booking.setStatus("PENDING");
            }
            if (booking.getUser() == null || booking.getUser().getId() == null) {
                return ResponseEntity.badRequest().body("Error: User information (ID) is missing in the request.");
            }
            Booking savedBooking = bookingRepository.save(booking);
            return ResponseEntity.ok(savedBooking);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Server Error: " + e.getMessage());
        }
    }

    // 2. GET ALL BOOKINGS (Admin)
    @GetMapping("/admin")
    public List<Booking> getAllBookingsForAdmin() {
        return bookingRepository.findAll();
    }

    // 3. GET SINGLE BOOKING
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable @NonNull Long id) {
        return bookingRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // VALET DASHBOARD ENDPOINTS
    // ==========================================

    // 4. GET VALET QUEUE
    @GetMapping("/valet/queue")
    public List<Booking> getValetQueue() {
        return bookingRepository.findByStatus("VALET_REQUESTED");
    }

    // 5. VALET ACCEPTS/PICKS UP CAR
    @PostMapping("/{id}/pickup")
    public ResponseEntity<?> pickupVehicle(@PathVariable @NonNull Long id) {
        return bookingRepository.findById(id).map(booking -> {
            booking.setStatus("VALET_PICKED_UP");
            return ResponseEntity.ok(bookingRepository.save(booking));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ✅ 6. NEW: VALET PARKS CAR (Handles Images & Location)
    @PostMapping(value = "/{id}/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> completeValetBooking(
            @PathVariable @NonNull Long id,
            @RequestParam("parkedLat") Double parkedLat,
            @RequestParam("parkedLng") Double parkedLng,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        return bookingRepository.findById(id).map(booking -> {
            try {
                // 1. Update Booking Data
                booking.setParkedLat(parkedLat);
                booking.setParkedLng(parkedLng);
                booking.setStatus("PARKED");

                // 2. Handle Image Uploads
                if (files != null && !files.isEmpty()) {
                    // Create upload directory if it doesn't exist
                    Path uploadPath = Paths.get("uploads");
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    List<String> uploadedUrls = new ArrayList<>();

                    for (MultipartFile file : files) {
                        // Generate unique filename
                        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                        Path filePath = uploadPath.resolve(fileName);
                        
                        // Save file to disk
                        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                        
                        // Create URL (Assuming you serve uploads from root)
                        String fileUrl = "http://localhost:8080/uploads/" + fileName;
                        uploadedUrls.add(fileUrl);
                    }

                    // Save the first image as the proof (if your model only supports one string)
                    // If you want to support multiple, you'd need to change your Entity to store a list or comma-separated string
                    if (!uploadedUrls.isEmpty()) {
                        booking.setParkedProofImage(uploadedUrls.get(0));
                    }
                }

                return ResponseEntity.ok(bookingRepository.save(booking));

            } catch (IOException e) {
                return ResponseEntity.internalServerError().body("Error saving images: " + e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // 7. VALET RELEASES CAR (User leaving)
    @PostMapping("/{id}/release")
    public ResponseEntity<?> releaseVehicle(@PathVariable @NonNull Long id) {
        return bookingRepository.findById(id).map(booking -> {
            booking.setStatus("COMPLETED");
            booking.setEndTime(LocalDateTime.now());
            return ResponseEntity.ok(bookingRepository.save(booking));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 8. GET BOOKINGS FOR A SPECIFIC USER
    @GetMapping("/user/{userId}")
    public List<Booking> getUserBookings(@PathVariable @NonNull Long userId) {
        return bookingRepository.findByUser_Id(userId);
    }

    // 9. UPDATE BOOKING LOCATION
    @PutMapping("/{id}/location")
    public ResponseEntity<?> updateLocation(@PathVariable @NonNull Long id, @RequestBody Booking locationData) {
        return bookingRepository.findById(id).map(booking -> {
            if (locationData.getPickupLat() != null) {
                booking.setPickupLat(locationData.getPickupLat());
            }
            if (locationData.getPickupLng() != null) {
                booking.setPickupLng(locationData.getPickupLng());
            }
            return ResponseEntity.ok(bookingRepository.save(booking));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 10. GET BOOKINGS BY LOT
    @GetMapping("/lot/{lotId}")
    public List<Booking> getBookingsByLot(@PathVariable @NonNull Long lotId) {
        return bookingRepository.findByLot_Id(lotId);
    }

    // 11. GET BOOKINGS BY VALET
    @GetMapping("/valet/{valetId}")
    public List<Booking> getBookingsByValet(@PathVariable @NonNull Long valetId) {
        return bookingRepository.findByValet_Id(valetId);
    }

    // 12. GET ACTIVE BOOKINGS BY STATUS AND VALET
    @GetMapping("/valet/{valetId}/status/{status}")
    public List<Booking> getValetBookingsByStatus(@PathVariable @NonNull Long valetId, @PathVariable String status) {
        return bookingRepository.findByStatusAndValet_Id(status, valetId);
    }

    @GetMapping("/earnings")
    public ResponseEntity<Double> getAdminEarnings(@RequestParam @NonNull Long adminId) {
        Double total = bookingRepository.calculateTotalEarningsForAdmin(adminId);
        return ResponseEntity.ok(total);
    }
}