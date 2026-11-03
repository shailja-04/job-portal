package com.jobportal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.jobportal.dto.AuthResponse;
import com.jobportal.dto.RegisterRequest;
import com.jobportal.model.AppUser;
import com.jobportal.model.Role;
import com.jobportal.repo.AppUserRepo;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserService userService;

    @Test
    @DisplayName("register should encode password, save user, and return AuthResponse")
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setName("New User");
        request.setEmail("newuser@jobportal.com");
        request.setPassword("plainPassword123");
        request.setRole(Role.SEEKER);

        AppUser savedUser = new AppUser();
        savedUser.setId(5);
        savedUser.setName("New User");
        savedUser.setEmail("newuser@jobportal.com");
        savedUser.setPassword("encoded_hash_xyz");
        savedUser.setRole(Role.SEEKER);

        when(userRepo.existsByEmail("newuser@jobportal.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword123")).thenReturn("encoded_hash_xyz");
        when(userRepo.save(any(AppUser.class))).thenReturn(savedUser);

        AuthResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals(5, response.getUserId());
        assertEquals("newuser@jobportal.com", response.getEmail());
        assertEquals(Role.SEEKER, response.getRole());
        verify(passwordEncoder, times(1)).encode("plainPassword123");
        verify(userRepo, times(1)).save(any(AppUser.class));
    }

    @Test
    @DisplayName("register should throw exception if email is already registered")
    void testRegister_DuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Duplicate User");
        request.setEmail("existing@jobportal.com");
        request.setPassword("password123");
        request.setRole(Role.POSTER);

        when(userRepo.existsByEmail("existing@jobportal.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.register(request);
        });

        assertTrue(ex.getMessage().contains("already registered"));
        verify(userRepo, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
