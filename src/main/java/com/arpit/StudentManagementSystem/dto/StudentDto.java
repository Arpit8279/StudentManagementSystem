package com.arpit.StudentManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDto {

    private Long id;
    private String course;
    private String name;
    private String email;
    private AddressResponseDto address;
    private DepartmentResponseDto department;
}
