package com.smartparking.dtos.response;

import com.smartparking.entities.nums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private String token;
    private Long userId;
    private String name;
    private String email;
    private Role role;
}
