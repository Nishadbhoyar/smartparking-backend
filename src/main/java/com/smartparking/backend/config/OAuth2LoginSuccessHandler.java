package com.smartparking.backend.config;

import com.smartparking.backend.model.User;
import com.smartparking.backend.repository.UserRepository;
import com.smartparking.backend.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    @Lazy
    private JwtUtil jwtUtil;

    @Autowired
    @Lazy
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");

            // 1. Fetch User
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found in database"));

            // 2. Update provider HERE (outside the authentication flow)
            if (!"GOOGLE".equals(user.getProvider())) {
                user.setProvider("GOOGLE");
                userRepository.save(user); // ✅ Safe to save here
            }

            // 3. Generate JWT
            String token = jwtUtil.generateToken(email);

            // 4. Redirect to React Frontend with Token
            String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/auth/callback")
                    .queryParam("token", token)
                    .queryParam("role", user.getRole())
                    .queryParam("id", user.getId().toString())
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("http://localhost:5173/login?error=oauth_failure");
        }
    }
}