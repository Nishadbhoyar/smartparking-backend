package com.smartparking.controller;

import com.smartparking.entities.nums.CarRentalStatus;
import com.smartparking.entities.nums.RentalCarStatus;
import com.smartparking.entities.nums.VehicleType;
import com.smartparking.entities.rental.CarRentalBooking;
import com.smartparking.entities.rental.RentalCar;
import com.smartparking.entities.users.Customer;
import com.smartparking.repositories.CarOwnerRepository;
import com.smartparking.repositories.CarRentalBookingRepository;
import com.smartparking.OtherServices.NotificationService;
import com.smartparking.repositories.CustomerRepository;
import com.smartparking.repositories.RentalCarRepository;
import com.smartparking.repositories.RentalCompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rental-cars")
public class RentalCarController {

    @Autowired private RentalCarRepository        rentalCarRepository;
    @Autowired private CarOwnerRepository         carOwnerRepository;
    @Autowired private RentalCompanyRepository     rentalCompanyRepository;
    @Autowired private CarRentalBookingRepository  carRentalBookingRepository;
    @Autowired private CustomerRepository          customerRepository;
    @Autowired private NotificationService          notificationService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // ─────────────────────────────────────────────────────────
    // CAR LISTING
    // ─────────────────────────────────────────────────────────

    // CarOwner lists a personal car
    @PostMapping("/owner/{ownerId}/list")
    public ResponseEntity<RentalCar> listCar(@PathVariable Long ownerId,
                                             @RequestBody RentalCar car) {
        var owner = carOwnerRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Car owner not found"));
        car.setCarOwner(owner);
        car.setStatus(RentalCarStatus.AVAILABLE);
        return new ResponseEntity<>(rentalCarRepository.save(car), HttpStatus.CREATED);
    }

    // FleetAdmin lists a company car
    @PostMapping("/company/{companyId}/list")
    public ResponseEntity<RentalCar> listCompanyCar(@PathVariable Long companyId,
                                                    @RequestBody RentalCar car) {
        var company = rentalCompanyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        car.setRentalCompany(company);
        car.setStatus(RentalCarStatus.AVAILABLE);
        return new ResponseEntity<>(rentalCarRepository.save(car), HttpStatus.CREATED);
    }

    // Get all cars owned by a CarOwner
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<RentalCar>> getOwnerCars(@PathVariable Long ownerId) {
        return ResponseEntity.ok(rentalCarRepository.findByCarOwnerId(ownerId));
    }

    // Get all cars in a company fleet
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<RentalCar>> getCompanyFleet(@PathVariable Long companyId) {
        return ResponseEntity.ok(rentalCarRepository.findByRentalCompanyId(companyId));
    }

    // Customer finds nearby available cars — optional vehicleType filter
    // Example: GET /api/rental-cars/nearby?lat=18.6&lng=73.8&type=BIKE
    @GetMapping("/nearby")
    public ResponseEntity<List<RentalCar>> getNearbyCars(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) VehicleType type) {

        List<RentalCar> cars = (type != null)
                ? rentalCarRepository.findNearbyAvailableCarsByType(lat, lng, type.name(), limit)
                : rentalCarRepository.findNearbyAvailableCars(lat, lng, limit);

        return ResponseEntity.ok(cars);
    }

    // Toggle car status
    @PutMapping("/{carId}/status")
    public ResponseEntity<RentalCar> updateStatus(@PathVariable Long carId,
                                                  @RequestParam RentalCarStatus status) {
        RentalCar car = rentalCarRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));
        car.setStatus(status);
        return ResponseEntity.ok(rentalCarRepository.save(car));
    }

    // ─────────────────────────────────────────────────────────
    // BOOKING
    // ─────────────────────────────────────────────────────────

    // Customer books a rental car
    @PostMapping("/{carId}/book")
    @Transactional
    public ResponseEntity<?> bookCar(@PathVariable Long carId,
                                     @RequestBody Map<String, Object> body) {

        if (body.get("customerId") == null ||
                body.get("startTime")  == null ||
                body.get("endTime")    == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "customerId, startTime and endTime are required"));
        }

        RentalCar car = rentalCarRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        if (car.getStatus() != RentalCarStatus.AVAILABLE) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Car is not available for booking"));
        }

        Long customerId = Long.valueOf(body.get("customerId").toString());
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        LocalDateTime startTime = LocalDateTime.parse(body.get("startTime").toString());
        LocalDateTime endTime   = LocalDateTime.parse(body.get("endTime").toString());

        if (!endTime.isAfter(startTime)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "End time must be after start time"));
        }

        long hours   = Duration.between(startTime, endTime).toHours();
        long days    = Math.max(1L, (long) Math.ceil(hours / 24.0));
        double total   = (car.getDailyRate()      != null ? car.getDailyRate()      : 0) * days;
        double deposit =  car.getSecurityDeposit() != null ? car.getSecurityDeposit() : 0;

        String code;
        do {
            code = "CR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (carRentalBookingRepository.findByBookingCode(code).isPresent());

        CarRentalBooking booking = new CarRentalBooking();
        booking.setBookingCode(code);
        booking.setCustomer(customer);
        booking.setRentalCar(car);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setTotalAmount(total);
        booking.setDepositAmount(deposit);
        booking.setStatus(CarRentalStatus.CONFIRMED);
        booking.setPickupOtp(String.format("%04d", SECURE_RANDOM.nextInt(10000)));
        booking.setReturnOtp(String.format("%04d",  SECURE_RANDOM.nextInt(10000)));
        booking.setOverdueAlerted(false);

        car.setStatus(RentalCarStatus.RENTED);
        rentalCarRepository.save(car);

        CarRentalBooking saved = carRentalBookingRepository.save(booking);

        // Notify customer: rental booking confirmed
        notificationService.notify(customerId,
                "Rental Confirmed ✅",
                "Booking " + saved.getBookingCode() + " for " + car.getMake()
                        + " " + (car.getModel() != null ? car.getModel() : "")
                        + " confirmed. Show your pickup OTP when collecting the car.",
                "RENTAL");
        // Notify car owner OR fleet admin that their car was booked
        if (car.getCarOwner() != null) {
            notificationService.notifyOwnerCarBooked(
                    car.getCarOwner().getId(),
                    saved.getBookingCode(),
                    car.getMake(),
                    customer.getName());
        } else if (car.getRentalCompany() != null && car.getRentalCompany().getFleetAdmin() != null) {
            notificationService.notifyFleetCarBooked(
                    car.getRentalCompany().getFleetAdmin().getId(),
                    saved.getBookingCode(),
                    car.getMake(),
                    customer.getName());
        }

        // Return pickupOtp to customer — they show this to owner at pickup
        // returnOtp is NOT returned here — owner will share it with customer at pickup
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.ofEntries(
                Map.entry("id",            saved.getId()),
                Map.entry("bookingCode",   saved.getBookingCode()),
                Map.entry("carMake",       car.getMake()),
                Map.entry("carModel",      car.getModel() != null ? car.getModel() : ""),
                Map.entry("vehicleType",   car.getVehicleType().name()),
                Map.entry("licensePlate",  car.getLicensePlate()),
                Map.entry("startTime",     saved.getStartTime().toString()),
                Map.entry("endTime",       saved.getEndTime().toString()),
                Map.entry("totalAmount",   saved.getTotalAmount()),
                Map.entry("depositAmount", saved.getDepositAmount()),
                Map.entry("status",        saved.getStatus().name()),
                Map.entry("pickupOtp",     saved.getPickupOtp())
        ));
    }

    // ─────────────────────────────────────────────────────────
    // FIX 1: VERIFY PICKUP OTP — owner confirms customer arrived
    // Owner calls this. Customer shows pickupOtp.
    // Booking: CONFIRMED → ACTIVE
    // ─────────────────────────────────────────────────────────
    @PostMapping("/bookings/{bookingId}/verify-pickup")
    @Transactional
    public ResponseEntity<?> verifyPickup(@PathVariable Long bookingId,
                                          @RequestBody Map<String, String> body) {
        String otp = body.get("otp");
        if (otp == null || otp.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "OTP is required"));
        }

        CarRentalBooking booking = carRentalBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != CarRentalStatus.CONFIRMED) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Booking is not in CONFIRMED state. Current: " + booking.getStatus()));
        }

        if (!otp.equals(booking.getPickupOtp())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid pickup OTP"));
        }

        booking.setStatus(CarRentalStatus.ACTIVE);
        booking.setActualPickupTime(LocalDateTime.now());
        carRentalBookingRepository.save(booking);

        // Notify customer: rental is now active
        notificationService.notify(booking.getCustomer().getId(),
                "Rental Active",
                "You have picked up " + booking.getRentalCar().getMake()
                        + ". Return by " + booking.getEndTime().toLocalDate() + ". Safe driving!",
                "RENTAL");
        // Notify car owner OR fleet admin
        RentalCar activeCar = booking.getRentalCar();
        if (activeCar.getCarOwner() != null) {
            notificationService.notifyOwnerCarPickedUp(
                    activeCar.getCarOwner().getId(),
                    activeCar.getMake(),
                    booking.getCustomer().getName());
        } else if (activeCar.getRentalCompany() != null && activeCar.getRentalCompany().getFleetAdmin() != null) {
            notificationService.notifyFleetCarPickedUp(
                    activeCar.getRentalCompany().getFleetAdmin().getId(),
                    activeCar.getMake(),
                    booking.getCustomer().getName());
        }

        // Return returnOtp to owner — they hand this to the customer now
        return ResponseEntity.ok(Map.of(
                "message",      "Pickup verified. Car is now ACTIVE.",
                "bookingId",    bookingId,
                "actualPickup", booking.getActualPickupTime().toString(),
                "returnOtp",    booking.getReturnOtp()  // owner hands this to customer for return
        ));
    }

    // ─────────────────────────────────────────────────────────
    // FIX 2: VERIFY RETURN OTP — owner confirms car returned
    // Owner calls this. Customer shows returnOtp.
    // Booking: ACTIVE → COMPLETED
    // If early return → refund calculated
    // ─────────────────────────────────────────────────────────
    @PostMapping("/bookings/{bookingId}/verify-return")
    @Transactional
    public ResponseEntity<?> verifyReturn(@PathVariable Long bookingId,
                                          @RequestBody Map<String, String> body) {
        String otp = body.get("otp");
        if (otp == null || otp.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "OTP is required"));
        }

        CarRentalBooking booking = carRentalBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != CarRentalStatus.ACTIVE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Booking is not ACTIVE. Current: " + booking.getStatus()));
        }

        if (!otp.equals(booking.getReturnOtp())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid return OTP"));
        }

        LocalDateTime now         = LocalDateTime.now();
        LocalDateTime bookedEnd   = booking.getEndTime();
        boolean isEarlyReturn     = now.isBefore(bookedEnd);

        // Recalculate refund if returned early
        double refundAmount = 0.0;
        if (isEarlyReturn && booking.getRentalCar().getDailyRate() != null) {
            long savedHours = Duration.between(now, bookedEnd).toHours();
            long savedDays  = savedHours / 24;
            if (savedDays > 0) {
                refundAmount = savedDays * booking.getRentalCar().getDailyRate();
            }
        }

        booking.setStatus(CarRentalStatus.COMPLETED);
        booking.setActualReturnTime(now);
        booking.setCompletedAt(now);

        // Mark car available again
        RentalCar car = booking.getRentalCar();
        car.setStatus(RentalCarStatus.AVAILABLE);
        rentalCarRepository.save(car);
        carRentalBookingRepository.save(booking);

        // Notify customer: rental completed
        String refundMsg = refundAmount > 0
                ? " Early return credit of Rs." + String.format("%.0f", refundAmount) + " will be processed."
                : "";
        notificationService.notify(booking.getCustomer().getId(),
                "Rental Completed",
                "Thanks for returning " + car.getMake() + "! Total: Rs."
                        + String.format("%.0f", booking.getTotalAmount()) + "." + refundMsg,
                "RENTAL");
        // Notify car owner OR fleet admin
        if (car.getCarOwner() != null) {
            notificationService.notifyOwnerCarReturned(
                    car.getCarOwner().getId(),
                    car.getMake(),
                    booking.getCustomer().getName(),
                    booking.getTotalAmount());
        } else if (car.getRentalCompany() != null && car.getRentalCompany().getFleetAdmin() != null) {
            notificationService.notifyFleetCarReturned(
                    car.getRentalCompany().getFleetAdmin().getId(),
                    car.getMake(),
                    booking.getCustomer().getName(),
                    booking.getTotalAmount());
        }

        return ResponseEntity.ok(Map.of(
                "message",           "Return verified. Booking COMPLETED.",
                "bookingId",         bookingId,
                "actualReturn",      now.toString(),
                "bookedEndTime",     bookedEnd.toString(),
                "earlyReturn",       isEarlyReturn,
                "refundAmount",      refundAmount,
                "refundNote",        refundAmount > 0
                        ? "Refund ₹" + refundAmount + " to customer for unused days"
                        : "No refund applicable"
        ));
    }

    // ─────────────────────────────────────────────────────────
    // FIX 3: EXTEND BOOKING TIME
    // Customer calls this before their endTime expires.
    // Checks no next booking conflicts, charges extra.
    // ─────────────────────────────────────────────────────────
    @PutMapping("/bookings/{bookingId}/extend")
    @Transactional
    public ResponseEntity<?> extendBooking(@PathVariable Long bookingId,
                                           @RequestBody Map<String, String> body) {
        String newEndTimeStr = body.get("newEndTime");
        if (newEndTimeStr == null || newEndTimeStr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "newEndTime is required"));
        }

        CarRentalBooking booking = carRentalBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != CarRentalStatus.ACTIVE &&
                booking.getStatus() != CarRentalStatus.CONFIRMED) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Can only extend ACTIVE or CONFIRMED bookings. Current: " + booking.getStatus()));
        }

        LocalDateTime newEndTime = LocalDateTime.parse(newEndTimeStr);
        LocalDateTime oldEndTime = booking.getEndTime();

        if (!newEndTime.isAfter(oldEndTime)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "New end time must be after current end time: " + oldEndTime));
        }

        // Check no other booking conflicts with the extended window
        boolean hasConflict = carRentalBookingRepository.hasOverlappingBooking(
                booking.getRentalCar().getId(),
                bookingId,
                oldEndTime,     // check from current end onwards
                newEndTime
        );

        if (hasConflict) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Cannot extend — another booking exists in that time window"));
        }

        // Calculate extra charge
        long extraHours = Duration.between(oldEndTime, newEndTime).toHours();
        long extraDays  = (long) Math.ceil(extraHours / 24.0);
        double extraCost = extraDays * (booking.getRentalCar().getDailyRate() != null
                ? booking.getRentalCar().getDailyRate() : 0);

        booking.setEndTime(newEndTime);
        booking.setTotalAmount(booking.getTotalAmount() + extraCost);
        carRentalBookingRepository.save(booking);

        // Notify customer: booking extended
        notificationService.notify(booking.getCustomer().getId(),
                "Rental Extended",
                "Your rental is extended to " + newEndTime.toLocalDate()
                        + ". Extra charge: Rs." + String.format("%.0f", extraCost) + ".",
                "RENTAL");
        // Notify car owner OR fleet admin
        RentalCar extendedCar = booking.getRentalCar();
        if (extendedCar.getCarOwner() != null) {
            notificationService.notify(extendedCar.getCarOwner().getId(),
                    "Rental Extended",
                    booking.getCustomer().getName() + " extended their rental of " + extendedCar.getMake()
                            + " to " + newEndTime.toLocalDate() + ".",
                    "RENTAL");
        } else if (extendedCar.getRentalCompany() != null && extendedCar.getRentalCompany().getFleetAdmin() != null) {
            notificationService.notify(extendedCar.getRentalCompany().getFleetAdmin().getId(),
                    "Fleet Rental Extended",
                    booking.getCustomer().getName() + " extended " + extendedCar.getMake()
                            + " to " + newEndTime.toLocalDate() + ".",
                    "RENTAL");
        }

        return ResponseEntity.ok(Map.of(
                "message",       "Booking extended successfully",
                "bookingId",     bookingId,
                "oldEndTime",    oldEndTime.toString(),
                "newEndTime",    newEndTime.toString(),
                "extraHours",    extraHours,
                "extraCost",     extraCost,
                "newTotalAmount", booking.getTotalAmount()
        ));
    }

    // ─────────────────────────────────────────────────────────
    // FIX 4: OWNER LIVE TRACKING DASHBOARD
    // Returns all current/upcoming/overdue bookings for owner's cars
    // ─────────────────────────────────────────────────────────
    @GetMapping("/owner/{ownerId}/active-rentals")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getOwnerActiveRentals(@PathVariable Long ownerId) {
        List<RentalCar> cars = rentalCarRepository.findByCarOwnerId(ownerId);
        LocalDateTime now = LocalDateTime.now();

        List<Map<String, Object>> result = cars.stream()
                .flatMap(car -> carRentalBookingRepository
                        .findByRentalCarIdAndStatus(car.getId(), CarRentalStatus.ACTIVE).stream())
                .map(b -> {
                    RentalCar c = b.getRentalCar();
                    boolean overdue = now.isAfter(b.getEndTime());
                    long minutesOverdue = overdue
                            ? Duration.between(b.getEndTime(), now).toMinutes() : 0;

                    Map<String, Object> entry = new HashMap<>();
                    entry.put("bookingId",       b.getId());
                    entry.put("bookingCode",      b.getBookingCode());
                    entry.put("carMake",          c.getMake());
                    entry.put("carModel",         c.getModel() != null ? c.getModel() : "");
                    entry.put("vehicleType",      c.getVehicleType().name());
                    entry.put("licensePlate",     c.getLicensePlate());
                    entry.put("customerName",     b.getCustomer().getName());
                    entry.put("startTime",        b.getStartTime() != null ? b.getStartTime().toString() : "");
                    entry.put("endTime",          b.getEndTime()   != null ? b.getEndTime().toString()   : "");
                    entry.put("actualPickupTime", b.getActualPickupTime() != null ? b.getActualPickupTime().toString() : "");
                    entry.put("status",           b.getStatus().name());
                    entry.put("isOverdue",        overdue);
                    entry.put("minutesOverdue",   minutesOverdue);
                    entry.put("totalAmount",      b.getTotalAmount() != null ? b.getTotalAmount() : 0);
                    return entry;
                })
                .sorted((a, b) -> Boolean.compare((Boolean) b.get("isOverdue"), (Boolean) a.get("isOverdue")))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ─────────────────────────────────────────────────────────
    // FIX 5: SCHEDULED OVERDUE ALERT — runs every 15 minutes
    // Flags bookings where car not returned past endTime
    // In production: send push notification / SMS / email here
    // ─────────────────────────────────────────────────────────
    @Scheduled(fixedRate = 900_000) // every 15 minutes
    @Transactional
    public void alertOverdueRentals() {
        List<CarRentalBooking> overdueBookings = carRentalBookingRepository
                .findByStatusAndEndTimeBeforeAndOverdueAlerted(
                        CarRentalStatus.ACTIVE,
                        LocalDateTime.now(),
                        false
                );

        for (CarRentalBooking booking : overdueBookings) {
            booking.setOverdueAlerted(true);
            carRentalBookingRepository.save(booking);
            // Notify customer: rental is overdue
            notificationService.notify(booking.getCustomer().getId(),
                    "Rental Overdue",
                    "Your rental of " + booking.getRentalCar().getMake()
                            + " was due on " + booking.getEndTime().toLocalDate()
                            + ". Please return the car to avoid extra charges.",
                    "RENTAL");
            // Notify owner OR fleet admin: their car is overdue
            RentalCar overdueCar = booking.getRentalCar();
            if (overdueCar.getCarOwner() != null) {
                notificationService.notifyOwnerCarOverdue(
                        overdueCar.getCarOwner().getId(),
                        overdueCar.getMake(),
                        booking.getCustomer().getName());
            } else if (overdueCar.getRentalCompany() != null && overdueCar.getRentalCompany().getFleetAdmin() != null) {
                notificationService.notifyFleetCarOverdue(
                        overdueCar.getRentalCompany().getFleetAdmin().getId(),
                        overdueCar.getMake(),
                        booking.getCustomer().getName());
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // BOOKING HISTORY VIEWS
    // ─────────────────────────────────────────────────────────

    @GetMapping("/owner/{ownerId}/bookings")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getOwnerBookings(@PathVariable Long ownerId) {
        List<RentalCar> cars = rentalCarRepository.findByCarOwnerId(ownerId);
        List<Map<String, Object>> result = cars.stream()
                .flatMap(car -> carRentalBookingRepository
                        .findByRentalCarIdOrderByCreatedAtDesc(car.getId()).stream())
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .map(b -> {
                    RentalCar c = b.getRentalCar();
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("id",               b.getId());
                    entry.put("bookingCode",       b.getBookingCode());
                    entry.put("carMake",           c.getMake());
                    entry.put("carModel",          c.getModel() != null ? c.getModel() : "");
                    entry.put("vehicleType",       c.getVehicleType().name());
                    entry.put("licensePlate",      c.getLicensePlate());
                    entry.put("customerName",      b.getCustomer().getName());
                    entry.put("startTime",         b.getStartTime() != null ? b.getStartTime().toString() : "");
                    entry.put("endTime",           b.getEndTime()   != null ? b.getEndTime().toString()   : "");
                    entry.put("actualPickupTime",  b.getActualPickupTime()  != null ? b.getActualPickupTime().toString()  : "");
                    entry.put("actualReturnTime",  b.getActualReturnTime()  != null ? b.getActualReturnTime().toString()  : "");
                    entry.put("totalAmount",       b.getTotalAmount()  != null ? b.getTotalAmount()  : 0);
                    entry.put("status",            b.getStatus().name());
                    entry.put("isOverdue",         b.getStatus() == CarRentalStatus.ACTIVE && LocalDateTime.now().isAfter(b.getEndTime()));
                    return entry;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/company/{companyId}/bookings")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getCompanyBookings(@PathVariable Long companyId) {
        List<RentalCar> cars = rentalCarRepository.findByRentalCompanyId(companyId);
        List<Map<String, Object>> result = cars.stream()
                .flatMap(car -> carRentalBookingRepository
                        .findByRentalCarIdOrderByCreatedAtDesc(car.getId()).stream())
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .map(b -> {
                    RentalCar c = b.getRentalCar();
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("id",               b.getId());
                    entry.put("bookingCode",       b.getBookingCode());
                    entry.put("carMake",           c.getMake());
                    entry.put("carModel",          c.getModel() != null ? c.getModel() : "");
                    entry.put("vehicleType",       c.getVehicleType().name());
                    entry.put("licensePlate",      c.getLicensePlate());
                    entry.put("customerName",      b.getCustomer().getName());
                    entry.put("startTime",         b.getStartTime() != null ? b.getStartTime().toString() : "");
                    entry.put("endTime",           b.getEndTime()   != null ? b.getEndTime().toString()   : "");
                    entry.put("actualPickupTime",  b.getActualPickupTime()  != null ? b.getActualPickupTime().toString()  : "");
                    entry.put("actualReturnTime",  b.getActualReturnTime()  != null ? b.getActualReturnTime().toString()  : "");
                    entry.put("totalAmount",       b.getTotalAmount()  != null ? b.getTotalAmount()  : 0);
                    entry.put("status",            b.getStatus().name());
                    return entry;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/customer/{customerId}/bookings")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getCustomerRentalBookings(
            @PathVariable Long customerId) {

        List<Map<String, Object>> result = carRentalBookingRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(b -> {
                    RentalCar c = b.getRentalCar();
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("id",              b.getId());
                    entry.put("bookingCode",     b.getBookingCode());
                    entry.put("carMake",         c.getMake());
                    entry.put("carModel",        c.getModel() != null ? c.getModel() : "");
                    entry.put("vehicleType",     c.getVehicleType().name());
                    entry.put("licensePlate",    c.getLicensePlate());
                    entry.put("startTime",       b.getStartTime() != null ? b.getStartTime().toString() : "");
                    entry.put("endTime",         b.getEndTime()   != null ? b.getEndTime().toString()   : "");
                    entry.put("totalAmount",     b.getTotalAmount()  != null ? b.getTotalAmount()  : 0);
                    entry.put("status",          b.getStatus().name());
                    entry.put("pickupOtp",       b.getPickupOtp() != null ? b.getPickupOtp() : "");
                    return entry;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}