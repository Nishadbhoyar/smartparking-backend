package com.smartparking.backend.service;

import com.smartparking.backend.model.User;
import com.smartparking.backend.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            // 1. Load User from Google
            OAuth2User oAuth2User = super.loadUser(userRequest);
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String email = (String) attributes.get("email");

            System.out.println("DEBUG: Google Login Email: " + email);

            // 2. Validate Email
            if (email == null) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("email_missing", "Email not found from provider", null));
            }

            // 3. Check Database
            User existingUser = userRepository.findByEmail(email).orElse(null);

            // 4. If User Not Found -> Block Login (Sign Up First)
            if (existingUser == null) {
                System.out.println("DEBUG: User not found. Blocking login.");
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("user_not_registered", "User not registered", null));
            }

            // 5. User found - success
            System.out.println("DEBUG: User found: " + existingUser.getEmail());
            return oAuth2User;

        } catch (OAuth2AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("server_error", "Internal Server Error", null));
        }
    }
}