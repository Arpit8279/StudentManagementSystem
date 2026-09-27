package com.arpit.StudentManagementSystem.service;

import com.arpit.StudentManagementSystem.dto.DepartmentRequestDto;
import com.arpit.StudentManagementSystem.dto.DepartmentResponseDto;

import java.util.List;

public interface DepartmentService {

    DepartmentResponseDto createDepartment(DepartmentRequestDto requestDto);
    List<DepartmentResponseDto> getAllDepartments();
    DepartmentResponseDto getDepartmentById(Long id);
}
