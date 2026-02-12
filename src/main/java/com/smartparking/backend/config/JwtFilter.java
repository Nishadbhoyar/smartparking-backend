package com.smartparking.backend.config;

import com.smartparking.backend.service.CustomUserDetailsService;
import com.smartparking.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    @Lazy
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        // 1. Skip filter for public endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/") || path.startsWith("/oauth2/")) {
            chain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        // 2. Extract Token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                email = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                // ❌ Token is malformed or tampered — reject immediately
                System.err.println("❌ [JwtFilter] Invalid Token: " + e.getMessage());
                sendUnauthorized(response, "Invalid or malformed JWT token.");
                return; // ✅ FIX: Stop the filter chain — don't let it fall through
            }
        }

        // 3. Validate and Set Auth
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

                if (jwtUtil.validateToken(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("✅ [JwtFilter] Authenticated User: " + email);
                } else {
                    // Token signature valid but expired or username mismatch
                    System.err.println("❌ [JwtFilter] Token validation failed for: " + email);
                    sendUnauthorized(response, "JWT token is expired or invalid.");
                    return; // ✅ FIX: Stop the filter chain
                }

            } catch (Exception e) {
                // ✅ FIX #2: User not found in DB (e.g., backend restarted and H2 data wiped).
                // Previously this silently fell through, letting Spring Security return a
                // generic 401. Now we return an explicit, descriptive 401 immediately.
                System.err.println("❌ [JwtFilter] User Not Found in DB for email: " + email);
                System.err.println("   Likely cause: backend restarted and H2 in-memory data was wiped.");
                System.err.println("   Fix: See application.properties — switch to file-based H2 or MySQL.");
                sendUnauthorized(response, "Session expired. Please sign up or log in again.");
                return; // ✅ FIX: Stop the filter chain
            }
        }

        chain.doFilter(request, response);
    }

    // ─── Helper ──────────────────────────────────────────────────────────────
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"error\": \"Unauthorized\", \"message\": \"" + message + "\"}");
    }
}