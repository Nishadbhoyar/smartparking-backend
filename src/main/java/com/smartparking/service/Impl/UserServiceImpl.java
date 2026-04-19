package com.smartparking.service.Impl;

import com.smartparking.dtos.request.UserRegistrationDTO;
import com.smartparking.dtos.response.UserResponseDTO;
import com.smartparking.entities.admins.CarOwner;
import com.smartparking.entities.admins.FleetAdmin;
import com.smartparking.entities.admins.ParkingLotAdmin;
import com.smartparking.entities.admins.SuperAdmin;
import com.smartparking.entities.users.Customer;
import com.smartparking.entities.users.User;
import com.smartparking.entities.valet.Valet;
import com.smartparking.exceptions.DuplicateResourceException;
import com.smartparking.exceptions.ResourceNotFoundException;
import com.smartparking.repositories.*;
import com.smartparking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParkingLotAdminRepository parkingLotAdminRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ValetRepository valetRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private CarOwnerRepository carOwnerRepository;

    @Autowired
    private FleetAdminRepository fleetAdminRepository;

    @Override
    public UserResponseDTO registerUser(UserRegistrationDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException(
                    "Registration failed: Email " + dto.getEmail() + " is already in use.");
        }

        User savedUser;

        switch (dto.getRole()) {
            case PARKING_LOT_ADMIN: {
                ParkingLotAdmin admin = new ParkingLotAdmin();
                mapCommonFields(admin, dto);
                admin.setBusinessRegistrationNumber(dto.getBusinessRegistrationNumber());
                savedUser = parkingLotAdminRepository.save(admin);
                break;
            }
            case SUPER_ADMIN: {
                SuperAdmin superAdmin = new SuperAdmin();
                mapCommonFields(superAdmin, dto);
                savedUser = superAdminRepository.save(superAdmin);
                break;
            }
            case CAR_OWNER: {
                CarOwner carOwner = new CarOwner();
                mapCommonFields(carOwner, dto);
                carOwner.setAadhaarNumber(dto.getAadhaarNumber());
                savedUser = carOwnerRepository.save(carOwner);
                break;
            }
            case FLEET_ADMIN: {
                FleetAdmin fleetAdmin = new FleetAdmin();
                mapCommonFields(fleetAdmin, dto);
                fleetAdmin.setBusinessPhone(dto.getBusinessPhone());
                savedUser = fleetAdminRepository.save(fleetAdmin);
                break;
            }
            case CUSTOMER: {
                Customer customer = new Customer();
                mapCommonFields(customer, dto);
                customer.setDefaultLicensePlate(dto.getDefaultLicensePlate());
                savedUser = customerRepository.save(customer);
                break;
            }
            case VALET: {
                Valet valet = new Valet();
                mapCommonFields(valet, dto);
                valet.setDrivingLicenseNumber(dto.getDrivingLicenseNumber());
                valet.setAvailableNow(true);
                savedUser = valetRepository.save(valet);
                break;
            }
            default:
                throw new IllegalArgumentException("Invalid Role provided!");
        }

        return mapToResponseDTO(savedUser);
    }

    @Override
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No user found with email: " + email));
        return mapToResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateProfile(Long userId, Map<String, String> body) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (body.containsKey("name") && !body.get("name").isBlank()) {
            user.setName(body.get("name").trim());
        }
        if (body.containsKey("password") && body.get("password").length() >= 6) {
            user.setPassword(passwordEncoder.encode(body.get("password")));
        }
        return mapToResponseDTO(userRepository.save(user));
    }

    private void mapCommonFields(User user, UserRegistrationDTO dto) {
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // BCrypt hashed
        user.setRole(dto.getRole());
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());

        if (user instanceof Customer) {
            dto.setDefaultLicensePlate(((Customer) user).getDefaultLicensePlate());
        } else if (user instanceof Valet) {
            dto.setDrivingLicenseNumber(((Valet) user).getDrivingLicenseNumber());
        }
        return dto;
    }
}