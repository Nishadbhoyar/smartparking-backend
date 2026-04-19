package com.smartparking.service.Impl;

import com.smartparking.dtos.response.ValetEarningsResponseDTO;
import com.smartparking.entities.valet.Valet;
import com.smartparking.entities.valet.ValetEarning;
import com.smartparking.exceptions.ResourceNotFoundException;
import com.smartparking.repositories.ValetEarningRepository;
import com.smartparking.repositories.ValetRepository;
import com.smartparking.service.ValetEarningsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValetEarningsServiceImpl implements ValetEarningsService {

    private static final double VALET_CUT_PERCENT    = 70.0;
    private static final double PLATFORM_CUT_PERCENT = 30.0;

    @Autowired private ValetEarningRepository earningRepository;
    @Autowired private ValetRepository        valetRepository;

    @Override
    public void recordEarning(Long valetId, Long valetRequestId, double totalFare) {
        Valet valet = valetRepository.findById(valetId)
                .orElseThrow(() -> new ResourceNotFoundException("Valet not found"));

        double valetCut    = round(totalFare * VALET_CUT_PERCENT    / 100.0);
        double platformCut = round(totalFare * PLATFORM_CUT_PERCENT / 100.0);

        earningRepository.save(ValetEarning.builder()
                .valet(valet)
                .valetRequestId(valetRequestId)
                .jobAmount(totalFare)
                .valetCut(valetCut)
                .platformCut(platformCut)
                .build());
    }

    @Override
    public ValetEarningsResponseDTO getEarningsDashboard(Long valetId) {
        Valet valet = valetRepository.findById(valetId)
                .orElseThrow(() -> new ResourceNotFoundException("Valet not found"));

        LocalDateTime startOfDay   = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay     = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime startOfWeek  = LocalDateTime.of(
                LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1),
                LocalTime.MIN);
        LocalDateTime startOfMonth = LocalDateTime.of(
                LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime now          = LocalDateTime.now();

        Double today   = earningRepository.sumEarningsByDateRange(valetId, startOfDay,  endOfDay);
        Double week    = earningRepository.sumEarningsByDateRange(valetId, startOfWeek, now);
        Double month   = earningRepository.sumEarningsByDateRange(valetId, startOfMonth, now);
        Double unpaid  = earningRepository.sumUnpaidEarnings(valetId);

        List<ValetEarning> recent = earningRepository.findByValetIdOrderByEarnedAtDesc(valetId);

        // FIX #8: use countByValetId so we get the real DB count, not just the list size
        long completedJobCount = earningRepository.countByValetId(valetId);

        List<ValetEarningsResponseDTO.EarningEntry> entries = recent.stream()
                .map(e -> ValetEarningsResponseDTO.EarningEntry.builder()
                        .jobId(e.getValetRequestId())
                        .jobAmount(e.getJobAmount())
                        .valetCut(e.getValetCut())
                        .platformCut(e.getPlatformCut())
                        .paid(e.isPaid())
                        .earnedAt(e.getEarnedAt())
                        .build())
                .collect(Collectors.toList());

        return ValetEarningsResponseDTO.builder()
                .valetId(valetId)
                .valetName(valet.getName())
                .totalEarningsToday(today   != null ? today   : 0.0)
                .totalEarningsThisWeek(week  != null ? week   : 0.0)
                .totalEarningsThisMonth(month!= null ? month  : 0.0)
                .totalUnpaidEarnings(unpaid  != null ? unpaid : 0.0)
                .totalJobsCompleted(completedJobCount) // FIX #8: was (long) recent.size()
                .recentEarnings(entries)
                .build();
    }

    @Override
    public void markAsPaid(Long valetId) {
        List<ValetEarning> unpaid = earningRepository.findByValetIdAndPaidFalse(valetId);
        unpaid.forEach(e -> { e.setPaid(true); e.setPaidAt(LocalDateTime.now()); });
        earningRepository.saveAll(unpaid);
    }

    private double round(double val) { return Math.round(val * 100.0) / 100.0; }
}