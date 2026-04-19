package com.smartparking.service.Impl;

import com.smartparking.OtherServices.SlotWebSocketService;
import com.smartparking.dtos.request.ValetBookingRequestDTO;
import com.smartparking.dtos.response.ValetResponseDTO;
import com.smartparking.entities.valet.ValetRequest;
import com.smartparking.entities.nums.SlotStatus;
import com.smartparking.entities.nums.ValetStatus;
import com.smartparking.entities.parking.ParkingLot;
import com.smartparking.entities.parking.Slot;
import com.smartparking.entities.users.Customer;
import com.smartparking.entities.valet.Valet;
import com.smartparking.exceptions.ResourceNotFoundException;
import com.smartparking.repositories.*;
import com.smartparking.OtherServices.NotificationService;
import java.util.List;
import com.smartparking.service.ValetEarningsService;
import com.smartparking.service.ValetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ValetServiceImpl implements ValetService {

    @Autowired
    private ValetRequestRepository valetRequestRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ValetRepository valetRepository;

    @Autowired
    private ParkingLotRepository parkingLotRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private SlotWebSocketService slotWebSocketService;

    @Autowired
    private ValetEarningsService valetEarningsService;

    @Autowired
    private ValetFareRepository valetFareRepository;

    @Autowired
    private NotificationService notificationService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private String generateOTP() {
        return String.format("%04d", SECURE_RANDOM.nextInt(10000));
    }

    @Override
    @Transactional
    public ValetResponseDTO requestValet(ValetBookingRequestDTO requestDTO) {
        Customer customer = customerRepository.findById(requestDTO.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found!"));

        ValetRequest request = new ValetRequest();
        request.setCustomer(customer);
        request.setCustomerName(customer.getName());
        request.setMobileNo(requestDTO.getMobileNo());
        request.setCarPlateNo(requestDTO.getCarPlateNo());
        request.setPickupLatitude(requestDTO.getPickupLatitude());
        request.setPickupLongitude(requestDTO.getPickupLongitude());
        request.setPickupOtp(generateOTP());
        request.setDropoffOtp(generateOTP());
        request.setStatus(ValetStatus.REQUESTED);
        request.setRequestedAt(LocalDateTime.now());

        ValetRequest saved = valetRequestRepository.save(request);
        // Notify customer: request submitted
        notificationService.notifyValetRequested(customer.getId());
        // Notify ALL available valets: new job posted
        valetRepository.findAllAvailableValets().forEach(v ->
                notificationService.notifyValetNewJobAvailable(v.getId(), customer.getName()));
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true) // FIX #5: lazy valet/lot/slot need open session in mapToResponseDTO
    public List<ValetResponseDTO> getAvailableJobs() {
        return valetRequestRepository.findByStatus(ValetStatus.REQUESTED)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ValetResponseDTO getActiveJob(Long valetId) {
        return valetRequestRepository.findFirstByValetIdAndStatusIn(
                valetId,
                List.of(ValetStatus.ACCEPTED, ValetStatus.PICKED_UP, ValetStatus.PARKED, ValetStatus.RETURN_REQUESTED)
        ).map(this::mapToResponseDTO).orElse(null);
    }

    @Override
    @Transactional
    public ValetResponseDTO acceptJob(Long requestId, Long valetId) {
        ValetRequest request = getRequest(requestId);

        if (request.getStatus() != ValetStatus.REQUESTED) {
            throw new RuntimeException("This job is no longer available.");
        }

        Valet valet = valetRepository.findById(valetId)
                .orElseThrow(() -> new ResourceNotFoundException("Valet not found!"));

        request.setValet(valet);
        request.setStatus(ValetStatus.ACCEPTED);

        ValetRequest saved = valetRequestRepository.save(request);
        // Notify customer: valet accepted and is on the way
        notificationService.notifyValetAccepted(request.getCustomer().getId(), valet.getName());
        // Notify other available valets: job is no longer available
        valetRepository.findAllAvailableValets().stream()
                .filter(v -> !v.getId().equals(valetId))
                .forEach(v -> notificationService.notifyValetJobTaken(v.getId()));
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional
    public ValetResponseDTO verifyPickup(Long requestId, String enteredOtp) {
        ValetRequest request = getRequest(requestId);

        if (request.getStatus() != ValetStatus.ACCEPTED) {
            throw new RuntimeException(
                    "Cannot verify pickup — job must be ACCEPTED first. Current status: "
                            + request.getStatus());
        }

        if (!request.getPickupOtp().equals(enteredOtp)) {
            throw new RuntimeException("Invalid Pickup OTP! Do not hand over the keys.");
        }

        request.setStatus(ValetStatus.PICKED_UP);
        ValetRequest pickedUp = valetRequestRepository.save(request);
        // Notify customer: valet has the keys and is heading to the lot
        notificationService.notifyCarPickedUp(
                request.getCustomer().getId(),
                request.getValet().getName());
        return mapToResponseDTO(pickedUp);
    }

    @Override
    @Transactional
    public ValetResponseDTO parkVehicle(Long requestId, Long lotId, Long slotId,
                                        List<MultipartFile> carImages) {
        ValetRequest request = valetRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Valet Request not found!"));

        if (request.getStatus() != ValetStatus.PICKED_UP) {
            throw new RuntimeException(
                    "Cannot park vehicle — OTP must be verified first. Current status: "
                            + request.getStatus());
        }

        ParkingLot lot = parkingLotRepository.findById(lotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found!"));
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found!"));

        // M-1 FIX: Verify the slot actually belongs to the given lot.
        if (!slot.getParkingLot().getId().equals(lotId)) {
            throw new IllegalArgumentException("Slot " + slotId + " does not belong to lot " + lotId + ".");
        }

        // M-1 FIX: Verify the slot is still available before marking it occupied.
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new RuntimeException("Slot " + slot.getSlotNumber() + " is already " + slot.getStatus() + ". Choose a different slot.");
        }

        request.setParkingLot(lot);
        request.setSlot(slot);
        slot.setStatus(SlotStatus.OCCUPIED);
        slotRepository.save(slot);
        slotWebSocketService.broadcastSlotUpdate(slot);

        List<String> savedImagePaths = new ArrayList<>();
        if (carImages != null && !carImages.isEmpty()) {

            if (carImages.size() > 5) {
                throw new IllegalArgumentException("Maximum 5 images allowed per valet request.");
            }

            final long MAX_SIZE_BYTES = 5 * 1024 * 1024L;
            final java.util.Set<String> ALLOWED_TYPES =
                    java.util.Set.of("image/jpeg", "image/png", "image/webp");

            String uploadDir = "uploads/valet_images/";
            File directory = new File(uploadDir);
            if (!directory.exists()) { directory.mkdirs(); }

            for (MultipartFile file : carImages) {
                if (file.isEmpty()) continue;
                if (file.getSize() > MAX_SIZE_BYTES)
                    throw new IllegalArgumentException(
                            "Image '" + file.getOriginalFilename() + "' exceeds 5 MB limit.");
                String ct = file.getContentType();
                if (ct == null || !ALLOWED_TYPES.contains(ct))
                    throw new IllegalArgumentException(
                            "Unsupported file type: " + ct + ". Only JPEG, PNG, WEBP allowed.");
                try {
                    // M-4 FIX: UUID-only filename — never use original filename.
                    // Original filename can contain path traversal (../../etc/passwd) or
                    // null bytes. We derive the extension from the validated content type only.
                    String ext = ct.equals("image/png") ? ".png" : ct.equals("image/webp") ? ".webp" : ".jpg";
                    String safeName = UUID.randomUUID().toString() + ext;
                    // FIX: both paths must be absolute before comparing with startsWith().
                    // Previously filePath was relative, uploadBase was absolute — a relative
                    // path can NEVER start with an absolute path, so this check ALWAYS threw
                    // SecurityException on every upload, giving a 400 for every file.
                    Path uploadBase = Paths.get(uploadDir).toAbsolutePath().normalize();
                    Path filePath   = uploadBase.resolve(safeName).normalize();
                    if (!filePath.startsWith(uploadBase)) {
                        throw new SecurityException("Path traversal attempt detected.");
                    }
                    Files.write(filePath, file.getBytes());
                    savedImagePaths.add(filePath.toString());
                } catch (SecurityException se) {
                    throw new IllegalArgumentException("Invalid file path.");
                } catch (Exception e) {
                    throw new RuntimeException("Failed to store image file.", e);
                }
            }
        }

        request.setCarImages(savedImagePaths);
        // Store the lot's coordinates as the parked location so the customer
        // can see exactly where their car is on a map.
        request.setParkedLatitude(lot.getLatitude());
        request.setParkedLongitude(lot.getLongitude());
        request.setStatus(ValetStatus.PARKED);
        request.setParkedAt(LocalDateTime.now());

        ValetRequest parkedReq = valetRequestRepository.save(request);
        // Notify customer: car is safely parked with lot + slot info
        notificationService.notifyCarParked(
                request.getCustomer().getId(),
                lot.getName(),
                slot.getSlotNumber());
        return mapToResponseDTO(parkedReq);
    }

    @Override
    @Transactional
    public ValetResponseDTO requestVehicleBack(Long requestId) {
        ValetRequest request = getRequest(requestId);

        if (request.getStatus() != ValetStatus.PARKED) {
            throw new RuntimeException(
                    "Cannot request vehicle return — current status is: " + request.getStatus());
        }

        request.setStatus(ValetStatus.RETURN_REQUESTED);
        ValetRequest returnReq = valetRequestRepository.save(request);
        // Notify VALET: customer wants their car back
        if (request.getValet() != null) {
            notificationService.notifyReturnRequested(
                    request.getValet().getId(),
                    request.getCustomerName());
        }
        return mapToResponseDTO(returnReq);
    }

    @Override
    @Transactional
    public ValetResponseDTO verifyDropoff(Long requestId, String enteredOtp) {
        ValetRequest request = getRequest(requestId);

        if (request.getStatus() != ValetStatus.RETURN_REQUESTED) {
            throw new RuntimeException(
                    "Cannot verify dropoff — customer must request return first. Current status: "
                            + request.getStatus());
        }

        if (!request.getDropoffOtp().equals(enteredOtp)) {
            throw new RuntimeException("Invalid Dropoff OTP! Transaction not completed.");
        }

        Slot slot = request.getSlot();
        if (slot != null) {
            slot.setStatus(SlotStatus.AVAILABLE);
            slotRepository.save(slot);
            slotWebSocketService.broadcastSlotUpdate(slot);
        }

        request.setStatus(ValetStatus.COMPLETED);
        request.setCompletedAt(LocalDateTime.now());

        ValetRequest saved = valetRequestRepository.save(request);
        // Notify customer: car has been returned, job done
        notificationService.notifyJobCompleted(
                request.getCustomer().getId(),
                request.getValet() != null ? request.getValet().getName() : "Your valet");

        if (request.getValet() != null) {
            valetFareRepository.findByValetRequestId(requestId).ifPresent(fare ->
                    valetEarningsService.recordEarning(
                            request.getValet().getId(),
                            requestId,
                            fare.getTotalFare()
                    )
            );
        }

        return mapToResponseDTO(saved);
    }

    private ValetRequest getRequest(Long id) {
        return valetRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Valet Request not found!"));
    }

    private ValetResponseDTO mapToResponseDTO(ValetRequest request) {
        ValetResponseDTO dto = new ValetResponseDTO();
        dto.setId(request.getId());
        dto.setCustomerName(request.getCustomerName());
        dto.setCarPlateNo(request.getCarPlateNo());
        dto.setStatus(request.getStatus());
        dto.setPickupOtp(request.getPickupOtp());
        dto.setDropoffOtp(request.getDropoffOtp());

        if (request.getValet() != null) {
            dto.setValetName(request.getValet().getName());
        }

        if (request.getParkingLot() != null) {
            dto.setParkingLotName(request.getParkingLot().getName());
            if (request.getSlot() != null) {
                dto.setSlotNumber(request.getSlot().getSlotNumber());
            }
            dto.setCarImages(request.getCarImages());
        }

        // Parked car location — available once status is PARKED or later
        dto.setParkedLatitude(request.getParkedLatitude());
        dto.setParkedLongitude(request.getParkedLongitude());

        return dto;
    }

    @Override
    @Transactional(readOnly = true) // FIX: missing annotation caused LazyInitializationException on valet/lot/slot fields
    public ValetResponseDTO getRequestById(Long requestId) {
        ValetRequest req = valetRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Valet request not found"));
        ValetResponseDTO dto = new ValetResponseDTO();
        dto.setId(req.getId());
        dto.setStatus(req.getStatus());
        dto.setCustomerName(req.getCustomerName());
        dto.setCarPlateNo(req.getCarPlateNo());
        if (req.getValet() != null) dto.setValetName(req.getValet().getName());
        if (req.getParkingLot() != null) dto.setParkingLotName(req.getParkingLot().getName());
        if (req.getSlot() != null) dto.setSlotNumber(req.getSlot().getSlotNumber());
        dto.setPickupOtp(req.getPickupOtp());
        dto.setDropoffOtp(req.getDropoffOtp());
        return dto;
    }

    /**
     * Returns the active valet request for a given customer.
     * "Active" means any status between REQUESTED and RETURN_REQUESTED inclusive.
     * Returns null if the customer has no active valet job.
     * Used by CustomerDashboard to show the live valet card.
     */
    @Override
    @Transactional(readOnly = true)
    public ValetResponseDTO getActiveValetForCustomer(Long customerId) {
        return valetRequestRepository.findFirstByCustomerIdAndStatusIn(
                customerId,
                List.of(ValetStatus.REQUESTED, ValetStatus.ACCEPTED,
                        ValetStatus.PICKED_UP, ValetStatus.PARKED,
                        ValetStatus.RETURN_REQUESTED)
        ).map(this::mapToResponseDTO).orElse(null);
    }

}