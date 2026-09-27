package com.arpit.StudentManagementSystem.service.impl;

import com.arpit.StudentManagementSystem.dto.LoginRequestDto;
import com.arpit.StudentManagementSystem.dto.LoginResponseDto;
import com.arpit.StudentManagementSystem.dto.RegisterRequestDto;
import com.arpit.StudentManagementSystem.entity.Department;
import com.arpit.StudentManagementSystem.entity.Role;
import com.arpit.StudentManagementSystem.entity.User;
import com.arpit.StudentManagementSystem.exception.DuplicateEmailException;
import com.arpit.StudentManagementSystem.repository.DepartmentRepository;
import com.arpit.StudentManagementSystem.repository.StudentRepository;
import com.arpit.StudentManagementSystem.repository.UserRepository;
import com.arpit.StudentManagementSystem.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register: success — saves student and user, returns message")
    void register_success() {
        RegisterRequestDto dto = new RegisterRequestDto(
                "arpit@test.com", "password123", "Arpit", "B.Tech", 1L, null);

        when(userRepository.existsByEmail("arpit@test.com")).thenReturn(false);
        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(new Department(1L, "CS")));
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(studentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String result = authService.register(dto);

        assertThat(result).contains("arpit@test.com");
        verify(studentRepository).save(any());
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("register: throws DuplicateEmailException when email already exists")
    void register_duplicateEmail_throwsException() {
        RegisterRequestDto dto = new RegisterRequestDto(
                "arpit@test.com", "password123", "Arpit", "B.Tech", 1L, null);

        when(userRepository.existsByEmail("arpit@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("arpit@test.com");

        verify(studentRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register: throws IllegalArgumentException when department not found")
    void register_departmentNotFound_throwsException() {
        RegisterRequestDto dto = new RegisterRequestDto(
                "arpit@test.com", "password123", "Arpit", "B.Tech", 999L, null);

        when(userRepository.existsByEmail("arpit@test.com")).thenReturn(false);
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("999");
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login: success — returns LoginResponseDto with token")
    void login_success() {
        LoginRequestDto dto = new LoginRequestDto("arpit@test.com", "password123");

        User user = new User(1L, "arpit@test.com", "hashed", Role.STUDENT, null);
        UserDetails mockUserDetails = mock(UserDetails.class);
        Authentication mockAuth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(mockAuth.getPrincipal()).thenReturn(mockUserDetails);
        when(mockUserDetails.getUsername()).thenReturn("arpit@test.com");
        when(userRepository.findByEmail("arpit@test.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("arpit@test.com", "STUDENT")).thenReturn("mock-jwt-token");

        LoginResponseDto result = authService.login(dto);

        assertThat(result.getToken()).isEqualTo("mock-jwt-token");
        assertThat(result.getEmail()).isEqualTo("arpit@test.com");
        assertThat(result.getRole()).isEqualTo("STUDENT");
    }

    @Test
    @DisplayName("login: throws exception on bad credentials")
    void login_badCredentials_throwsException() {
        LoginRequestDto dto = new LoginRequestDto("arpit@test.com", "wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(BadCredentialsException.class);
    }
}
