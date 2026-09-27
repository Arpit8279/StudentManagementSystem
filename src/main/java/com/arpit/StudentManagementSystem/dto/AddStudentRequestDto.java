package com.arpit.StudentManagementSystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AddStudentRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    private String password;

    @NotBlank(message = "Course is required")
    private String course;

    @Valid
    private AddressRequestDto addressRequestDto;

    @NotNull(message = "Department ID is required")
    private Long departmentId;
}