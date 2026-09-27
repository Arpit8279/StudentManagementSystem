package com.arpit.StudentManagementSystem.service.impl;

import com.arpit.StudentManagementSystem.dto.DepartmentRequestDto;
import com.arpit.StudentManagementSystem.dto.DepartmentResponseDto;
import com.arpit.StudentManagementSystem.entity.Department;
import com.arpit.StudentManagementSystem.repository.DepartmentRepository;
import com.arpit.StudentManagementSystem.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;


    @Override
    public DepartmentResponseDto createDepartment(DepartmentRequestDto requestDto) {
        log.info("Creating department with name: {}", requestDto.getName());
        Department department = new Department();
        department.setName(requestDto.getName());
        Department savedDepartment = departmentRepository.save(department);
        log.info("Department created with id: {}", savedDepartment.getId());
        return new DepartmentResponseDto(savedDepartment.getId(), savedDepartment.getName());
    }

    @Override
    public List<DepartmentResponseDto> getAllDepartments() {
        log.debug("Fetching all departments");
        List<DepartmentResponseDto> dtos = departmentRepository.findAll().stream()
                .map(department -> new DepartmentResponseDto(department.getId(), department.getName()))
                .toList();
        log.debug("Found {} departments", dtos.size());
        return dtos;
    }

    @Override
    public DepartmentResponseDto getDepartmentById(Long id) {
        log.debug("Fetching department with id: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Department not found with id: {}", id);
                    return new RuntimeException("Department not found");
                });
        return new DepartmentResponseDto(department.getId(), department.getName());
    }
}
