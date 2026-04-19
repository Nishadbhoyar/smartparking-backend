package com.smartparking.service.Impl;

import com.smartparking.dtos.DashboardOverviewDTO;
import com.smartparking.dtos.FinancialOverviewDTO;
import com.smartparking.dtos.response.PlatformDashboardResponseDTO;
import com.smartparking.entities.nums.BookingStatus;
import com.smartparking.entities.nums.SlotStatus;
import com.smartparking.entities.nums.ValetStatus;
import com.smartparking.repositories.*;
import com.smartparking.repositories.ParkingLotRepository;
import com.smartparking.repositories.UserRepository;
import com.smartparking.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;   // ← FIX: was missing, caused lines 128 + 140 errors

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired private SlotRepository         slotRepository;
    @Autowired private BookingRepository      bookingRepository;
    @Autowired private ValetRequestRepository valetRequestRepository;
    @Autowired private ValetRepository        valetRepository;
    @Autowired private ValetEarningRepository valetEarningRepository;
    @Autowired private UserRepository         userRepository;
    @Autowired private ParkingLotRepository   parkingLotRepository;

    @Override
    public DashboardOverviewDTO getDashboardOverview(Long lotId) {
        DashboardOverviewDTO overview = new DashboardOverviewDTO();
        overview.setTotalSlots(slotRepository.countByParkingLotId(lotId));
        overview.setOccupiedSlots(slotRepository.countByParkingLotIdAndStatus(lotId, SlotStatus.OCCUPIED));
        overview.setAvailableSlots(slotRepository.countByParkingLotIdAndStatus(lotId, SlotStatus.AVAILABLE));
        overview.setMaintenanceSlots(slotRepository.countByParkingLotIdAndStatus(lotId, SlotStatus.MAINTENANCE));

        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay   = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        Double todaysEarnings = bookingRepository.calculateEarningsByDateRange(
                lotId, BookingStatus.COMPLETED, startOfDay, endOfDay);
        overview.setTodaysEarnings(todaysEarnings != null ? todaysEarnings : 0.0);
        return overview;
    }

    @Override
    public FinancialOverviewDTO getFinancialOverview(Long lotId) {
        FinancialOverviewDTO finance = new FinancialOverviewDTO();
        Double totalRev = bookingRepository.calculateTotalRevenue(lotId, BookingStatus.COMPLETED);
        totalRev = (totalRev != null) ? totalRev : 0.0;
        finance.setTotalRevenue(totalRev);
        long transactions = bookingRepository.countByParkingLotIdAndStatus(lotId, BookingStatus.COMPLETED);
        finance.setTotalTransactions(transactions);
        if (transactions > 0) {
            finance.setAverageTransactionValue(Math.round((totalRev / transactions) * 100.0) / 100.0);
        } else {
            finance.setAverageTransactionValue(0.0);
        }
        return finance;
    }

    @Override
    public PlatformDashboardResponseDTO getPlatformDashboard() {

        LocalDateTime startOfDay   = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay     = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime startOfWeek  = LocalDateTime.of(
                LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1),
                LocalTime.MIN);
        LocalDateTime startOfMonth = LocalDateTime.of(
                LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime now = LocalDateTime.now();

        Double revToday = bookingRepository.calculatePlatformEarningsByDateRange(
                BookingStatus.COMPLETED, startOfDay, endOfDay);
        Double revWeek  = bookingRepository.calculatePlatformEarningsByDateRange(
                BookingStatus.COMPLETED, startOfWeek, now);
        Double revMonth = bookingRepository.calculatePlatformEarningsByDateRange(
                BookingStatus.COMPLETED, startOfMonth, now);
        Double revAll   = bookingRepository.calculatePlatformTotalRevenue(BookingStatus.COMPLETED);

        Long bookingsToday     = bookingRepository.countByStatusAndEntryTimeBetween(
                BookingStatus.COMPLETED, startOfDay, endOfDay);
        Long activeBookings    = bookingRepository.countByStatus(BookingStatus.ACTIVE);
        Long completedBookings = bookingRepository.countByStatus(BookingStatus.COMPLETED);

        Long valetRequestsToday = valetRequestRepository.countByStatusAndRequestedAtBetween(
                ValetStatus.COMPLETED, startOfDay, endOfDay);
        Long activeValets = valetRepository.countByIsAvailableNowTrue();

        Long totalSlots     = slotRepository.count();
        Long occupiedSlots  = slotRepository.countByStatus(SlotStatus.OCCUPIED);
        Long availableSlots = slotRepository.countByStatus(SlotStatus.AVAILABLE);

        // FIX: Collectors now imported above — this was causing lines 128 + 140 errors
        List<PlatformDashboardResponseDTO.TopLotDTO> topLots =
                bookingRepository.findTopLotsByRevenue(BookingStatus.COMPLETED)
                        .stream().limit(3)
                        .map(row -> PlatformDashboardResponseDTO.TopLotDTO.builder()
                                .lotId(((Number) row[0]).longValue())
                                .lotName((String) row[1])
                                .revenue(((Number) row[2]).doubleValue())
                                .bookings(((Number) row[3]).longValue())
                                .build())
                        .collect(Collectors.toList());

        // FIX: findTopValetsByEarnings() now exists in ValetEarningRepository
        List<PlatformDashboardResponseDTO.TopValetDTO> topValets =
                valetEarningRepository.findTopValetsByEarnings()
                        .stream().limit(3)
                        .map(row -> PlatformDashboardResponseDTO.TopValetDTO.builder()
                                .valetId(((Number) row[0]).longValue())
                                .valetName((String) row[1])
                                .jobsCompleted(((Number) row[2]).longValue())
                                .totalEarned(((Number) row[3]).doubleValue())
                                .build())
                        .collect(Collectors.toList());

        return PlatformDashboardResponseDTO.builder()
                .totalUsers(userRepository.count())
                .totalParkingLots(parkingLotRepository.count())
                .totalRevenueToday(nvl(revToday))
                .totalRevenueThisWeek(nvl(revWeek))
                .totalRevenueThisMonth(nvl(revMonth))
                .totalRevenueAllTime(nvl(revAll))
                .totalBookingsToday(nvl(bookingsToday))
                .totalActiveBookings(nvl(activeBookings))
                .totalCompletedBookings(nvl(completedBookings))
                .totalValetRequestsToday(nvl(valetRequestsToday))
                .activeValets(nvl(activeValets))
                .totalSlots(nvl(totalSlots))
                .occupiedSlots(nvl(occupiedSlots))
                .availableSlots(nvl(availableSlots))
                .topParkingLots(topLots)
                .topValets(topValets)
                .build();
    }

    private Double nvl(Double v) { return v != null ? v : 0.0; }
    private Long   nvl(Long   v) { return v != null ? v : 0L;  }
}