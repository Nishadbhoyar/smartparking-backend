package com.smartparking.service.Impl;

import com.smartparking.dtos.request.ParkingLotRequestDTO;
import com.smartparking.dtos.response.ParkingLotResponseDTO;
import com.smartparking.entities.admins.ParkingLotAdmin;
import com.smartparking.entities.nums.ParkingLotStatus;
import com.smartparking.entities.nums.Role;
import com.smartparking.entities.parking.Feature;
import com.smartparking.entities.parking.ParkingLot;

import com.smartparking.exceptions.ResourceNotFoundException;
import com.smartparking.exceptions.UnauthorizedAccessException;

import com.smartparking.repositories.FeatureRepository;
import com.smartparking.repositories.ParkingLotAdminRepository;
import com.smartparking.repositories.ParkingLotRepository;
import com.smartparking.service.ParkingLotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParkingLotServiceImpl implements ParkingLotService {

    @Autowired
    private ParkingLotRepository parkingLotRepository;

    @Autowired
    private ParkingLotAdminRepository parkingLotAdminRepository;

    @Autowired
    private FeatureRepository featureRepository;

    @Override
    @Transactional
    public ParkingLotResponseDTO createParkingLot(ParkingLotRequestDTO requestDTO) {
        ParkingLotAdmin parkingLotAdmin = parkingLotAdminRepository.findById(requestDTO.getAdminId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Admin not found with ID: " + requestDTO.getAdminId()));

        if (parkingLotAdmin.getRole() != Role.PARKING_LOT_ADMIN) {
            throw new UnauthorizedAccessException(
                    "Security Alert: Only ADMIN users can create parking lots!");
        }

        ParkingLot lot = new ParkingLot();
        lot.setName(requestDTO.getName());
        lot.setLatitude(requestDTO.getLatitude());
        lot.setLongitude(requestDTO.getLongitude());
        lot.setParkingLotAdmin(parkingLotAdmin);
        lot.setStatus(ParkingLotStatus.ACTIVE);
        lot.setCompanyVerified(false);

        ParkingLot savedLot = parkingLotRepository.save(lot);
        return mapToResponseDTO(savedLot);
    }

    @Override
    @Transactional(readOnly = true) // FIX #1: session kept open so lazy parkingLotAdmin can load
    public List<ParkingLotResponseDTO> getLotsByAdmin(Long adminId) {
        return parkingLotRepository.findByParkingLotAdminId(adminId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParkingLotResponseDTO updateParkingLot(Long lotId, Long adminId, ParkingLotRequestDTO requestDTO) {
        ParkingLot lot = parkingLotRepository.findByIdAndParkingLotAdminId(lotId, adminId)
                .orElseThrow(() -> new UnauthorizedAccessException(
                        "Access Denied: You do not own this parking lot or it does not exist."));

        lot.setName(requestDTO.getName());
        lot.setLatitude(requestDTO.getLatitude());
        lot.setLongitude(requestDTO.getLongitude());

        return mapToResponseDTO(parkingLotRepository.save(lot));
    }

    @Override
    @Transactional
    public void deleteParkingLot(Long lotId, Long adminId) {
        ParkingLot lot = parkingLotRepository.findByIdAndParkingLotAdminId(lotId, adminId)
                .orElseThrow(() -> new UnauthorizedAccessException(
                        "Access Denied: You do not own this parking lot or it does not exist."));
        parkingLotRepository.delete(lot);
    }

    @Override
    @Transactional
    public ParkingLotResponseDTO verifyCompanyLot(Long lotId, boolean isVerified) {
        ParkingLot lot = parkingLotRepository.findById(lotId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Parking lot with ID " + lotId + " not found."));

        if (isVerified) {
            List<String> featureNames = lot.getFeatures().stream()
                    .map(Feature::getName)
                    .collect(Collectors.toList());

            if (!featureNames.contains("CCTV") || !featureNames.contains("24/7 Security")) {
                throw new RuntimeException(
                        "Verification Failed: This lot does not meet company standards. " +
                                "It requires 'CCTV' and '24/7 Security'.");
            }
        }

        lot.setCompanyVerified(isVerified);
        return mapToResponseDTO(parkingLotRepository.save(lot));
    }

    @Override
    @Transactional
    public ParkingLotResponseDTO updateLotStatus(Long lotId, Long adminId, ParkingLotStatus status) {
        ParkingLot lot = parkingLotRepository.findByIdAndParkingLotAdminId(lotId, adminId)
                .orElseThrow(() -> new UnauthorizedAccessException(
                        "Access Denied: You do not own this parking lot."));
        lot.setStatus(status);
        return mapToResponseDTO(parkingLotRepository.save(lot));
    }

    @Override
    @Transactional
    public ParkingLotResponseDTO addFeatureToLot(Long lotId, Long adminId, Long featureId) {
        ParkingLot lot = parkingLotRepository.findByIdAndParkingLotAdminId(lotId, adminId)
                .orElseThrow(() -> new UnauthorizedAccessException(
                        "Access Denied: You do not own this parking lot."));

        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found."));

        lot.getFeatures().add(feature);
        return mapToResponseDTO(parkingLotRepository.save(lot));
    }

    @Override
    @Transactional(readOnly = true) // FIX #2: session kept open so lazy parkingLotAdmin can load
    public ParkingLotResponseDTO getLotById(Long lotId) {
        ParkingLot lot = parkingLotRepository.findById(lotId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Parking lot with ID " + lotId + " not found."));
        return mapToResponseDTO(lot);
    }

    @Override
    @Transactional(readOnly = true) // FIX #3: session kept open so lazy parkingLotAdmin can load
    public List<ParkingLotResponseDTO> getNearbyLots(double latitude, double longitude, int limit) {
        return parkingLotRepository.findNearbyActiveLots(latitude, longitude, limit)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private ParkingLotResponseDTO mapToResponseDTO(ParkingLot lot) {
        ParkingLotResponseDTO dto = new ParkingLotResponseDTO();
        dto.setId(lot.getId());
        dto.setName(lot.getName());
        dto.setLatitude(lot.getLatitude());
        dto.setLongitude(lot.getLongitude());
        dto.setAdminId(lot.getParkingLotAdmin().getId());
        dto.setAdminName(lot.getParkingLotAdmin().getName());
        dto.setCompanyVerified(lot.isCompanyVerified());
        dto.setStatus(lot.getStatus());
        dto.setFeatures(lot.getFeatures().stream()
                .map(Feature::getName)
                .collect(Collectors.toList()));
        return dto;
    }
}