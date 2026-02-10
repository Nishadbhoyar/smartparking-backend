package com.smartparking.backend.controller;

import com.smartparking.backend.model.User;
import com.smartparking.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // 👈 Add this so we can hash passwords

    // 1. Register a new user
    @PostMapping("/register")
    public User registerUser(@RequestBody @NonNull User user) {
        return userRepository.save(user);
    }

    // 2. Get all users (for testing)
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 3. Login Endpoint (Manual Login)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        // 1. Get the email from the currently logged-in user
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getName();

        // 2. Use that email to find the user
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (user == null || !user.getPassword().equals(loginUser.getPassword())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        return ResponseEntity.ok(response);
    }

    // ✅ 4. GET CURRENT USER (Fixed: Returns Database User)
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "Not authenticated"));
        }

        // 1. Get email from Google Session
        String email = principal.getAttribute("email");

        // 2. Fetch the REAL user from Database (so we get the Phone Number & Role)
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    // ✅ 5. UPDATE USER PROFILE (Supports phone number, name, and role updates)
    // ✅ 5. UPDATE USER PROFILE
    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(@AuthenticationPrincipal OAuth2User principal,
            @RequestBody Map<String, String> updates) {
        if (principal == null)
            return ResponseEntity.status(401).build();

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (user == null)
            return ResponseEntity.notFound().build();

        // Update Phone
        if (updates.containsKey("phoneNumber")) {
            user.setPhoneNumber(updates.get("phoneNumber"));
        }
        // Update Name
        if (updates.containsKey("name")) {
            user.setName(updates.get("name"));
        }
        // Update Role
        if (updates.containsKey("role")) {
            user.setRole(updates.get("role"));
        }

        // ✅ NEW: Add Password (hashed)
        if (updates.containsKey("password") && !updates.get("password").isEmpty()) {
            user.setPassword(passwordEncoder.encode(updates.get("password")));
        }

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
}