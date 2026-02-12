package com.smartparking.backend.controller;

import com.smartparking.backend.model.User;
import com.smartparking.backend.repository.UserRepository;
import com.smartparking.backend.service.OtpService;
import com.smartparking.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder; // 👈 WAS MISSING
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
    private JwtUtil jwtUtil;

    // ✅ ADDED: PasswordEncoder to handle hashing
    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==========================================
    // 1. SEND OTP
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
    // 2. VERIFY OTP
    // ==========================================
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        if (!otpService.validateOtp(email, otp)) {
            return ResponseEntity.status(401).body("Error: Invalid or Expired OTP");
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName("New User");
            user.setRole("DRIVER");
            user.setProvider("EMAIL_OTP");
            // ✅ FIX: Hash the default password
            user.setPassword(passwordEncoder.encode("OTP-LOGIN"));
            userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(buildLoginResponse(user, token));
    }

    // ==========================================
    // 3. MANUAL LOGIN
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body("Error: User not found");
        }

        // ✅ FIX: Use matches() to compare plain text vs Hash
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Error: Invalid Password");
        }

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

        // ✅ FIX: Hash password before saving
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        user.setRole(signupRequest.getRole());
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

        // ✅ FIX: Hash the new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully! Login now."));
    }

    // ==========================================
    // 6. MAGIC LINK ENDPOINTS
    // ==========================================
    @PostMapping("/send-magic-link")
    public ResponseEntity<?> sendMagicLink(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        otpService.generateAndSendMagicLink(email);
        return ResponseEntity.ok(Map.of("message", "Magic link sent! Check your email."));
    }

    @PostMapping("/verify-magic-link")
    public ResponseEntity<?> verifyMagicLink(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String email = otpService.validateMagicToken(token);
        if (email == null) {
            return ResponseEntity.status(401).body("Error: Invalid or Expired Link");
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName("New User");
            user.setRole("DRIVER");
            user.setProvider("MAGIC_LINK");
            user.setPassword(passwordEncoder.encode("")); // Hash empty
            userRepository.save(user);
        }

        String jwt = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(buildLoginResponse(user, jwt));
    }

    // ==========================================
    // 7. LOGOUT (Added to satisfy Frontend)
    // ==========================================
    @PostMapping("/logout") // 👈 WAS MISSING
    public ResponseEntity<?> logout() {
        // Since we use Stateless JWT, the server doesn't need to do anything.
        // The client simply deletes the token.
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // Helper
    private Map<String, Object> buildLoginResponse(User user, String token) {
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole());
        response.put("user", userData);
        return response;
    }
}