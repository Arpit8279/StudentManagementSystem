package com.arpit.StudentManagementSystem.service;

import com.arpit.StudentManagementSystem.dto.AddStudentRequestDto;
import com.arpit.StudentManagementSystem.dto.StudentDto;
import com.arpit.StudentManagementSystem.dto.StudentProjection;
import com.arpit.StudentManagementSystem.dto.StudentSummaryDto;

import java.util.List;

public interface StudentService {

    // ── CRUD Operations ───────────────────────────────────────────────────────
    StudentDto createStudent(AddStudentRequestDto addStudentRequestDto);
    List<StudentDto> getAllstudent();
    StudentDto getStudent(Long id);
    void deleteStudentById(Long id);
    StudentDto updateStudent(Long id, AddStudentRequestDto addStudentRequestDto);
    StudentDto partialupdateStudent(Long id, AddStudentRequestDto addStudentRequestDto);

    // ── 1. Derived Query Methods ──────────────────────────────────────────────
    List<StudentDto> getStudentsByCourse(String course);
    List<StudentDto> searchStudentsByName(String name);
    List<StudentDto> getStudentsByDepartmentName(String departmentName);
    List<StudentDto> getStudentsByCity(String city);

    // ── 2. JPA (JPQL) Query Methods ───────────────────────────────────────────
    List<StudentDto> getStudentsByDepartmentNameJpql(String departmentName);
    List<StudentDto> getStudentsByCourseAndCityJpql(String course, String city);
    List<StudentSummaryDto> getStudentSummariesByCourseJpql(String course);

    // ── 3. Native Query Methods ───────────────────────────────────────────────
    List<StudentDto> getStudentsByEmailDomainNative(String domain);
    List<StudentDto> getStudentsByCityNative(String city);
    Long getStudentCountByDepartmentNative(Long departmentId);

    // ── 4. Projection Methods ─────────────────────────────────────────────────
    List<StudentProjection> getStudentProjectionsByCourse(String course);
    List<StudentProjection> getStudentProjectionsByCourseNative(String course);
}
