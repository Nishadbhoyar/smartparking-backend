package com.smartparking.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String home() {
        return "Smart Parking Backend is Running Successfully!";
    }

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    @GetMapping("/test-oauth")
    public String testOAuth() {
        if (clientRegistrationRepository == null) {
            return "❌ OAuth2 ClientRegistrationRepository is NULL - OAuth2 NOT configured!";
        }

        try {
            var registration = clientRegistrationRepository.findByRegistrationId("google");
            if (registration != null) {
                return "✅ OAuth2 IS configured! Client ID: " + registration.getClientId();
            }
            return "❌ Google registration not found in repository!";
        } catch (Exception e) {
            return "❌ Error checking OAuth2: " + e.getMessage();
        }
    }
}