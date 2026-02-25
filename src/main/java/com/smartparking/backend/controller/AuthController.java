package com.smartparking.backend.controller;

import com.smartparking.backend.model.User;
import com.smartparking.backend.repository.UserRepository;
import com.smartparking.backend.service.OtpService;
import com.smartparking.backend.util.JwtUtil;

import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    // 2. VERIFY OTP (Account Activation)
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
            return ResponseEntity.badRequest().body("Error: User not found. Please sign up first.");
        }

        // ✅ NEW: The OTP is correct! Unlock the account.
        user.setVerified(true);
        userRepository.save(user);

        // Optional: Log them in immediately after verifying
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

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Error: Invalid Password");
        }

        // ✅ NEW: Block login if they haven't done the OTP step yet
        if (!user.isVerified()) {
            return ResponseEntity.status(403).body("Error: Please verify your email with the OTP before logging in.");
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
        
        // Hash password before saving
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        
        user.setRole(signupRequest.getRole());
        user.setPhoneNumber(signupRequest.getPhoneNumber());
        user.setProvider("EMAIL");
        
        // ✅ NEW: Lock their account until they verify the OTP
        user.setVerified(false); 

        userRepository.save(user);

        // ✅ NEW: Automatically trigger the OTP email!
        try {
            otpService.generateAndSendOtp(user.getEmail());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("User registered, but failed to send OTP: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of("message", "Registration successful! Please check your email for the OTP."));
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
    public ResponseEntity<?> sendMagicLink(@RequestBody Map<String, String> request) throws MessagingException {
        String email = request.get("email");

        // 1. Check if user exists in DB
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // User not found: Return error and DO NOT send email
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Email not registered. Please sign up first."));
        }

        // 2. User exists: Generate and send link
        otpService.generateAndSendMagicLink(email);

        return ResponseEntity.ok(Map.of("success", true, "message", "Magic link sent! Check your email."));
    }

    @PostMapping("/verify-magic-link")
    public ResponseEntity<?> verifyMagicLink(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String email = otpService.validateMagicToken(token);

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Invalid or Expired Link");
        }

        User user = userRepository.findByEmail(email).orElse(null);

        // 1. Strict Login Check (Do not create new user)
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User account not found.");
        }

        String jwt = jwtUtil.generateToken(user.getEmail());

        // 2. Determine Dashboard URL based on Role
        String redirectUrl = "/dashboard"; // Default fallback
        String role = user.getRole();

        if ("DRIVER".equalsIgnoreCase(role)) {
            redirectUrl = "/driver/dashboard";
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            redirectUrl = "/admin/dashboard";
        } else if ("USER".equalsIgnoreCase(role)) {
            redirectUrl = "/user/home";
        }

        // 3. Construct Response with Redirect URL
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("user", user);
        response.put("redirectUrl", redirectUrl); // <--- Frontend needs this

        return ResponseEntity.ok(response);
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