package com.arpit.StudentManagementSystem.controller;

import com.arpit.StudentManagementSystem.dto.LoginRequestDto;
import com.arpit.StudentManagementSystem.dto.LoginResponseDto;
import com.arpit.StudentManagementSystem.dto.RegisterRequestDto;
import com.arpit.StudentManagementSystem.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Register and login — public endpoints (no token required)")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a new student account",
            description = "Creates a Student profile and a User login account. Role is hardcoded to STUDENT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registered successfully",
                    content = @Content(schema = @Schema(example = "Student registered successfully with email: arpit@test.com"))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email already registered", content = @Content)
    })
    @SecurityRequirements   // Override global bearer requirement — this endpoint is public
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        String message = authService.register(registerRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @Operation(summary = "Login and get JWT token",
            description = "Authenticate with email + password. Returns a Bearer token to use in all other requests.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful — copy the token and use 'Authorize' above",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    })
    @SecurityRequirements   // Override global bearer requirement — this endpoint is public
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto response = authService.login(loginRequestDto);
        return ResponseEntity.ok(response);
    }
}
