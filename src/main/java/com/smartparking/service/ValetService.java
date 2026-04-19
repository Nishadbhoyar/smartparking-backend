package com.smartparking.service;

import com.smartparking.dtos.request.ValetBookingRequestDTO;
import com.smartparking.dtos.response.ValetResponseDTO;

import java.util.List;

public interface ValetService {

    ValetResponseDTO getRequestById(Long requestId);

    ValetResponseDTO requestValet(ValetBookingRequestDTO requestDTO);

    List<ValetResponseDTO> getAvailableJobs();

    ValetResponseDTO getActiveJob(Long valetId);

    ValetResponseDTO acceptJob(Long requestId, Long valetId);

    ValetResponseDTO verifyPickup(Long requestId, String enteredOtp);

    ValetResponseDTO parkVehicle(Long requestId, Long lotId, Long slotId,
                                 List<org.springframework.web.multipart.MultipartFile> carImages);

    ValetResponseDTO requestVehicleBack(Long requestId);

    ValetResponseDTO verifyDropoff(Long requestId, String enteredOtp);

    // NEW: customer dashboard needs the active valet job for this customer (if any)
    ValetResponseDTO getActiveValetForCustomer(Long customerId);
}