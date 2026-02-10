package com.smartparking.backend.controller;

import com.smartparking.backend.model.User;
import com.smartparking.backend.repository.UserRepository;
import com.smartparking.backend.service.OtpService;
import com.smartparking.backend.util.JwtUtil; // ✅ Ensure this Import exists

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private JwtUtil jwtUtil; // ✅ Inject JWT Utility

    // ==========================================
    // 1. SEND OTP (EMAIL)
    // ==========================================
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || !email.contains("@")) {
            return ResponseEntity.badRequest().body("Error: Invalid Email Address");
        }

        try {
            otpService.generateAndSendOtp(email);
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully to " + email));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error sending OTP: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. VERIFY OTP & LOGIN (Returns JWT)
    // ==========================================
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        // 1. Validate OTP
        if (!otpService.validateOtp(email, otp)) {
            return ResponseEntity.status(401).body("Error: Invalid or Expired OTP");
        }

        // 2. Auto-Register or Get User
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName("New User");
            user.setRole("DRIVER"); // Default Role
            user.setProvider("EMAIL_OTP");
            user.setPassword("OTP-LOGIN");
            userRepository.save(user);
        }

        // 3. 🚀 GENERATE TOKEN
        String token = jwtUtil.generateToken(user.getEmail());

        // 4. Return Token + User Info
        return ResponseEntity.ok(buildLoginResponse(user, token));
    }

    // ==========================================
    // 3. MANUAL LOGIN (Password -> Returns JWT)
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);

        if (user == null || !user.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.status(401).body("Error: Invalid Email or Password");
        }

        // 🚀 GENERATE TOKEN
        String token = jwtUtil.generateToken(user.getEmail());

        return ResponseEntity.ok(buildLoginResponse(user, token));
    }

    // ==========================================
    // 4. SIGNUP
    // ==========================================
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User signupRequest) {
        if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        User user = new User();
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(signupRequest.getPassword());
        user.setRole(signupRequest.getRole());

        // ✅ FIX: Save the Phone Number
        // Note: Ensure your User.java model has getPhoneNumber()
        user.setPhoneNumber(signupRequest.getPhoneNumber());

        user.setProvider("EMAIL");

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Registration successful! Please login."));
    }

    // ==========================================
    // 5. RESET PASSWORD
    // ==========================================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        if (!otpService.validateOtp(email, otp)) {
            return ResponseEntity.status(401).body("Error: Invalid OTP");
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null)
            return ResponseEntity.badRequest().body("Error: User not found");

        user.setPassword(newPassword);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully! Login now."));
    }
    // ... inside AuthController

    // ==========================================
    // 6. SEND MAGIC LINK
    // ==========================================
    @PostMapping("/send-magic-link")
    public ResponseEntity<?> sendMagicLink(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        otpService.generateAndSendMagicLink(email);
        return ResponseEntity.ok(Map.of("message", "Magic link sent! Check your email."));
    }

    // ==========================================
    // 7. VERIFY MAGIC LINK
    // ==========================================
    @PostMapping("/verify-magic-link")
    public ResponseEntity<?> verifyMagicLink(@RequestBody Map<String, String> request) {
        String token = request.get("token");

        // 1. Validate Token -> Get Email
        String email = otpService.validateMagicToken(token);
        if (email == null) {
            return ResponseEntity.status(401).body("Error: Invalid or Expired Link");
        }

        // 2. Find or Create User (Same logic as OTP)
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName("New User");
            user.setRole("DRIVER");
            user.setProvider("MAGIC_LINK");
            user.setPassword("");
            userRepository.save(user);
        }

        // 3. Generate JWT
        String jwt = jwtUtil.generateToken(user.getEmail());

        return ResponseEntity.ok(buildLoginResponse(user, jwt));
    }

    // ==========================================
    // HELPER: Build JSON Response
    // ==========================================
    private Map<String, Object> buildLoginResponse(User user, String token) {
        Map<String, Object> response = new HashMap<>();
        response.put("token", token); // ✅ Returns the JWT Token

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole());

        response.put("user", userData);
        return response;
    }
}