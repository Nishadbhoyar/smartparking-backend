package com.smartparking.security;

import com.smartparking.entities.users.User;
import com.smartparking.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Bridges Spring Security's UserDetailsService with our User entity.
 * Spring Security calls loadUserByUsername (by convention) — we use email as the username.
 *
 * FIX: Added @Cacheable so the DB is only queried ONCE per email per session,
 * instead of on every single API request.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));

        // Roles must be prefixed with ROLE_ for Spring Security's hasRole() checks
        String roleAuthority = "ROLE_" + user.getRole().name();

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(roleAuthority))
        );
    }
}