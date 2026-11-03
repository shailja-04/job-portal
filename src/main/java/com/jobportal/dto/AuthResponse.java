package com.jobportal.dto;

import com.jobportal.model.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String message;
    private int userId;
    private String name;
    private String email;
    private Role role;
}
