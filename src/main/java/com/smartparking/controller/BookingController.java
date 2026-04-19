package com.smartparking.controller;

import com.smartparking.dtos.request.BookingRequestDTO;
import com.smartparking.dtos.response.BookingResponseDTO;
import com.smartparking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/reserve")
    public ResponseEntity<BookingResponseDTO> reserveSlot(@RequestBody BookingRequestDTO requestDTO) {
        return new ResponseEntity<>(bookingService.createBooking(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponseDTO> cancelBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BookingResponseDTO>> getCustomerBookings(@PathVariable Long customerId) {
        return new ResponseEntity<>(bookingService.getBookingsByCustomer(customerId), HttpStatus.OK);
    }

    @PostMapping("/verify-code")
    public ResponseEntity<BookingResponseDTO> verifyEntryCode(
            @RequestParam String code,
            @RequestParam Long lotId) {
        return new ResponseEntity<>(bookingService.verifyEntryCode(code, lotId), HttpStatus.OK);
    }

    @PostMapping("/checkout")
    public ResponseEntity<BookingResponseDTO> checkoutBooking(@RequestParam String code) {
        return new ResponseEntity<>(bookingService.checkoutBooking(code), HttpStatus.OK);
    }

    @GetMapping("/lot/{lotId}")
    public ResponseEntity<List<BookingResponseDTO>> getLotBookings(@PathVariable Long lotId) {
        return new ResponseEntity<>(bookingService.getBookingsByLot(lotId), HttpStatus.OK);
    }

    // NEW — was missing, caused the 500
    @GetMapping("/lot-admin/{adminId}")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByAdmin(@PathVariable Long adminId) {
        return new ResponseEntity<>(bookingService.getBookingsByAdmin(adminId), HttpStatus.OK);
    }
}