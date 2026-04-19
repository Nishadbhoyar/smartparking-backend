package com.smartparking.service;

import com.smartparking.dtos.request.ParkingLotRequestDTO;
import com.smartparking.dtos.response.ParkingLotResponseDTO;
import com.smartparking.entities.nums.ParkingLotStatus;

import java.util.List;

public interface ParkingLotService {

    ParkingLotResponseDTO createParkingLot(ParkingLotRequestDTO requestDTO);

    List<ParkingLotResponseDTO> getLotsByAdmin(Long adminId);

    ParkingLotResponseDTO updateParkingLot(Long lotId, Long adminId, ParkingLotRequestDTO requestDTO);

    void deleteParkingLot(Long lotId, Long adminId);

    ParkingLotResponseDTO verifyCompanyLot(Long lotId, boolean isVerified);

    // H-06 FIX: adminId added — ownership must be verified before changing status
    ParkingLotResponseDTO updateLotStatus(Long lotId, Long adminId, ParkingLotStatus status);

    // H-06 FIX: adminId added — ownership must be verified before adding a feature
    ParkingLotResponseDTO addFeatureToLot(Long lotId, Long adminId, Long featureId);

    ParkingLotResponseDTO getLotById(Long lotId);

    List<ParkingLotResponseDTO> getNearbyLots(double latitude, double longitude, int limit);
}