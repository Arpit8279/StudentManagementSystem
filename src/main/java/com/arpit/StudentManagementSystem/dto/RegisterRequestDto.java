package com.arpit.StudentManagementSystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {

    // Role is intentionally NOT accepted from the client.
    // It is hardcoded to STUDENT in the service layer for security.

    // --- User (login) fields ---
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    // --- Student (profile) fields ---
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Course is required")
    private String course;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @Valid
    private AddressRequestDto addressRequestDto;
}
