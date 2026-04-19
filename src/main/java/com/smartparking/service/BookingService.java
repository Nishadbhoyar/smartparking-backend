package com.smartparking.service;

import com.smartparking.dtos.request.BookingRequestDTO;
import com.smartparking.dtos.response.BookingResponseDTO;
import java.util.List;

public interface BookingService {
    BookingResponseDTO createBooking(BookingRequestDTO requestDTO);
    List<BookingResponseDTO> getBookingsByCustomer(Long customerId);
    BookingResponseDTO verifyEntryCode(String bookingCode, Long parkingLotId);
    BookingResponseDTO checkoutBooking(String bookingCode);
    List<BookingResponseDTO> getBookingsByLot(Long lotId);
    List<BookingResponseDTO> getBookingsByAdmin(Long adminId);
    BookingResponseDTO cancelBooking(Long bookingId);
    BookingResponseDTO getBookingById(Long bookingId);
}