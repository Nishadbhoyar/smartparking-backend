package com.smartparking.service;

import com.smartparking.dtos.request.UserRegistrationDTO;
import com.smartparking.dtos.response.UserResponseDTO;
import java.util.Map;

public interface UserService {
    UserResponseDTO registerUser(UserRegistrationDTO requestDTO);
    UserResponseDTO getUserByEmail(String email);
    UserResponseDTO updateProfile(Long userId, Map<String, String> body);
}