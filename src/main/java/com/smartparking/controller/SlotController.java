package com.smartparking.controller;

import com.smartparking.dtos.request.BulkSlotRequestDTO;
import com.smartparking.dtos.request.SlotRequestDTO;
import com.smartparking.dtos.response.SlotResponseDTO;
import com.smartparking.entities.nums.SlotStatus;
import com.smartparking.service.SlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slots")

public class SlotController {

    @Autowired
    private SlotService slotService;

    // POST /api/slots/add
    @PostMapping("/add")
    public ResponseEntity<SlotResponseDTO> addSlot(@RequestBody SlotRequestDTO requestDTO) {
        SlotResponseDTO savedSlot = slotService.createSlot(requestDTO);
        return new ResponseEntity<>(savedSlot, HttpStatus.CREATED);
    }

    // GET /api/slots/lot/{lotId}
    @GetMapping("/lot/{lotId}")
    public ResponseEntity<List<SlotResponseDTO>> getSlotsByLot(@PathVariable Long lotId) {
        return new ResponseEntity<>(slotService.getSlotsByLot(lotId), HttpStatus.OK);
    }

    // GET /api/slots/lot/{lotId}/available  — slots with status AVAILABLE
    @GetMapping("/lot/{lotId}/available")
    public ResponseEntity<List<SlotResponseDTO>> getAvailableSlots(@PathVariable Long lotId) {
        List<SlotResponseDTO> all = slotService.getSlotsByLot(lotId);
        List<SlotResponseDTO> available = all.stream()
                .filter(s -> s.getStatus() == SlotStatus.AVAILABLE)
                .toList();
        return new ResponseEntity<>(available, HttpStatus.OK);
    }

    // PUT /api/slots/{slotId}/status?status=AVAILABLE
    // Frontend calls: PUT /api/slots/{slotId}/status?status=OCCUPIED
    @PutMapping("/{slotId}/status")
    public ResponseEntity<SlotResponseDTO> updateSlotStatus(
            @PathVariable Long slotId,
            @RequestParam SlotStatus status) {
        SlotResponseDTO updated = slotService.updateSlotStatus(slotId, status);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    // DELETE /api/slots/{slotId}
    @DeleteMapping("/{slotId}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long slotId) {
        slotService.deleteSlot(slotId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // POST /api/slots/bulk-generate
    @PostMapping("/bulk-generate")
    public ResponseEntity<String> bulkGenerateSlots(@RequestBody BulkSlotRequestDTO request) {
        String responseMessage = slotService.bulkGenerateSlots(request);
        return new ResponseEntity<>(responseMessage, HttpStatus.CREATED);
    }
}
