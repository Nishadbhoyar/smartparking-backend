package com.smartparking.service;

import com.smartparking.dtos.request.SlotRequestDTO;
import com.smartparking.dtos.response.SlotResponseDTO;
import com.smartparking.entities.nums.SlotStatus;
import java.util.List;

public interface SlotService {
    SlotResponseDTO createSlot(SlotRequestDTO requestDTO);
    List<SlotResponseDTO> getSlotsByLot(Long lotId);
    SlotResponseDTO updateSlotStatus(Long slotId, SlotStatus newStatus);
    void deleteSlot(Long slotId);
    String bulkGenerateSlots(com.smartparking.dtos.request.BulkSlotRequestDTO request);
}
