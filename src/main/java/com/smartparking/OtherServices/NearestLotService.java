package com.smartparking.OtherServices;

import com.smartparking.entities.parking.ParkingLot;
import com.smartparking.entities.parking.Slot;
import com.smartparking.repositories.ParkingLotRepository;
import com.smartparking.repositories.SlotRepository;
import com.smartparking.utils.GeoUtils;
import com.smartparking.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class NearestLotService {

    // Only search within 15 km of user's location
    private static final double MAX_LOT_SEARCH_RADIUS_KM = 15.0;

    private final ParkingLotRepository parkingLotRepository;
    private final SlotRepository slotRepository;

    public NearestLotService(ParkingLotRepository parkingLotRepository,
                             SlotRepository slotRepository) {
        this.parkingLotRepository = parkingLotRepository;
        this.slotRepository = slotRepository;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  CORE METHOD: Find nearest verified lot with available slots
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Given the user's pickup location, finds the nearest VERIFIED parking lot
     * that has at least one AVAILABLE slot.
     *
     * "Verified" means the parking lot has been approved by the platform
     * with security features (CCTV, guard, etc.)
     *
     * Steps:
     * 1. Get all ACTIVE/VERIFIED parking lots
     * 2. Filter to only lots with available slots
     * 3. Sort by distance from user's pickup location
     * 4. Return the nearest one
     *
     * @param userLat  User's pickup latitude
     * @param userLon  User's pickup longitude
     * @return NearestLotResult containing lot + nearest available slot
     */
    public NearestLotResult findNearestAvailableLot(double userLat, double userLon) {

        // Get all verified/active lots
        // Assumes ParkingLot has a 'status' field with ACTIVE value
        List<ParkingLot> verifiedLots =
                parkingLotRepository.findByStatus(
                        com.smartparking.entities.nums.ParkingLotStatus.ACTIVE
                );

        if (verifiedLots.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No verified parking lots found on the platform."
            );
        }

        // Find nearest lot that has available slots
        Optional<NearestLotResult> result = verifiedLots.stream()
                // Only lots within search radius
                .filter(lot -> lot.getLatitude() != null && lot.getLongitude() != null)
                .filter(lot -> GeoUtils.isWithinRadius(
                        userLat, userLon,
                        lot.getLatitude(), lot.getLongitude(),
                        MAX_LOT_SEARCH_RADIUS_KM
                ))
                // Map to result with distance
                .map(lot -> {
                    double distKm = GeoUtils.calculateDistanceKm(
                            userLat, userLon,
                            lot.getLatitude(), lot.getLongitude()
                    );
                    // Find first available slot in this lot
                    Optional<Slot> availableSlot = slotRepository
                            .findFirstByParkingLotIdAndStatus(
                                    lot.getId(),
                                    com.smartparking.entities.nums.SlotStatus.AVAILABLE
                            );
                    return new NearestLotResult(lot, availableSlot.orElse(null), distKm);
                })
                // Only lots that have at least one available slot
                .filter(r -> r.availableSlot() != null)
                // Pick the nearest
                .min(Comparator.comparingDouble(NearestLotResult::distanceKm));

        return result.orElseThrow(() ->
                new ResourceNotFoundException(
                        "No parking lots with available slots found within " +
                                MAX_LOT_SEARCH_RADIUS_KM + " km. " +
                                "All nearby lots are full."
                )
        );
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Get top 3 nearest lots — show user options before confirming
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Returns top 3 nearest available lots so the user/system can choose.
     * Could be used to show the user "Here are the lots we'll park your car at"
     */
    public List<NearestLotResult> findTop3NearestLots(double userLat, double userLon) {
        List<ParkingLot> verifiedLots =
                parkingLotRepository.findByStatus(
                        com.smartparking.entities.nums.ParkingLotStatus.ACTIVE
                );

        return verifiedLots.stream()
                .filter(lot -> lot.getLatitude() != null && lot.getLongitude() != null)
                .filter(lot -> GeoUtils.isWithinRadius(
                        userLat, userLon,
                        lot.getLatitude(), lot.getLongitude(),
                        MAX_LOT_SEARCH_RADIUS_KM
                ))
                .map(lot -> {
                    double distKm = GeoUtils.calculateDistanceKm(
                            userLat, userLon,
                            lot.getLatitude(), lot.getLongitude()
                    );
                    Optional<Slot> slot = slotRepository.findFirstByParkingLotIdAndStatus(
                            lot.getId(),
                            com.smartparking.entities.nums.SlotStatus.AVAILABLE
                    );
                    return new NearestLotResult(lot, slot.orElse(null), distKm);
                })
                .filter(r -> r.availableSlot() != null)
                .sorted(Comparator.comparingDouble(NearestLotResult::distanceKm))
                .limit(3)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Result record
    // ─────────────────────────────────────────────────────────────────────

    public record NearestLotResult(
            ParkingLot parkingLot,
            Slot availableSlot,
            double distanceKm
    ) {
        public int etaMinutes() {
            return GeoUtils.estimatedMinutes(distanceKm);
        }
    }
}