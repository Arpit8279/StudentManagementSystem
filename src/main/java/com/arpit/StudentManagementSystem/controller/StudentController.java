package com.arpit.StudentManagementSystem.controller;

import com.arpit.StudentManagementSystem.dto.AddStudentRequestDto;
import com.arpit.StudentManagementSystem.dto.StudentDto;
import com.arpit.StudentManagementSystem.dto.StudentProjection;
import com.arpit.StudentManagementSystem.dto.StudentSummaryDto;
import com.arpit.StudentManagementSystem.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
@Tag(name = "Students", description = "Manage student records — CRUD operations, derived queries, JPQL, native queries, and projections")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // =========================================================================
    // ── CRUD OPERATIONS ──────────────────────────────────────────────────────
    // =========================================================================

    @Operation(summary = "Create a student", description = "Admin-only: creates a new student record directly (without creating a user account)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Student created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or department not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied — requires ADMIN role", content = @Content)
    })
    @PostMapping
    public ResponseEntity<StudentDto> createStudent(@Valid @RequestBody AddStudentRequestDto dto) {
        StudentDto studentDto = studentService.createStudent(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(studentDto);
    }

    @Operation(summary = "Get all students", description = "Returns a list of all registered students")
    @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    @GetMapping
    public ResponseEntity<List<StudentDto>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllstudent());
    }

    @Operation(summary = "Get student by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student found"),
            @ApiResponse(responseCode = "404", description = "Student not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> getStudentById(
            @Parameter(description = "ID of the student to retrieve") @PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudent(id));
    }

    @Operation(summary = "Full update of a student (PUT)", description = "Replaces all student fields. Requires all fields to be provided.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student updated"),
            @ApiResponse(responseCode = "404", description = "Student not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<StudentDto> updateStudent(
            @Parameter(description = "ID of the student to update") @PathVariable Long id,
            @Valid @RequestBody AddStudentRequestDto dto) {
        return ResponseEntity.ok(studentService.updateStudent(id, dto));
    }

    @Operation(summary = "Partial update of a student (PATCH)", description = "Updates only the fields provided in the request body.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student updated"),
            @ApiResponse(responseCode = "404", description = "Student not found", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<StudentDto> partialUpdateStudent(
            @Parameter(description = "ID of the student to partially update") @PathVariable Long id,
            @RequestBody AddStudentRequestDto dto) {
        return ResponseEntity.ok(studentService.partialupdateStudent(id, dto));
    }

    @Operation(summary = "Delete a student", description = "Admin-only: permanently deletes a student record")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Student deleted"),
            @ApiResponse(responseCode = "404", description = "Student not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied — requires ADMIN role", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(
            @Parameter(description = "ID of the student to delete") @PathVariable Long id) {
        studentService.deleteStudentById(id);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // ── 1. DERIVED QUERY METHODS ─────────────────────────────────────────────
    // =========================================================================

    @Operation(summary = "Find students by course (Derived Query)",
            description = "Uses a Spring Data JPA derived query: findByCourseIgnoreCase")
    @ApiResponse(responseCode = "200", description = "Matching students retrieved successfully")
    @GetMapping("/search/by-course")
    public ResponseEntity<List<StudentDto>> getStudentsByCourse(
            @Parameter(description = "Course name (e.g. B.Tech, MCA)") @RequestParam String course) {
        return ResponseEntity.ok(studentService.getStudentsByCourse(course));
    }

    @Operation(summary = "Search students by name (Derived Query)",
            description = "Uses a Spring Data JPA derived query: findByNameContainingIgnoreCase (LIKE %name%)")
    @ApiResponse(responseCode = "200", description = "Matching students retrieved successfully")
    @GetMapping("/search/by-name")
    public ResponseEntity<List<StudentDto>> searchStudentsByName(
            @Parameter(description = "Keyword to match against student name") @RequestParam String name) {
        return ResponseEntity.ok(studentService.searchStudentsByName(name));
    }

    @Operation(summary = "Find students by department name (Derived Query)",
            description = "Uses a Spring Data JPA derived query traversing association: findByDepartmentNameIgnoreCase")
    @ApiResponse(responseCode = "200", description = "Matching students retrieved successfully")
    @GetMapping("/search/by-department")
    public ResponseEntity<List<StudentDto>> getStudentsByDepartmentName(
            @Parameter(description = "Department name (e.g. Computer Science)") @RequestParam String departmentName) {
        return ResponseEntity.ok(studentService.getStudentsByDepartmentName(departmentName));
    }

    @Operation(summary = "Find students by address city (Derived Query)",
            description = "Uses a Spring Data JPA derived query traversing association: findByAddressCityIgnoreCase")
    @ApiResponse(responseCode = "200", description = "Matching students retrieved successfully")
    @GetMapping("/search/by-city")
    public ResponseEntity<List<StudentDto>> getStudentsByCity(
            @Parameter(description = "City name (e.g. Bangalore, Mumbai)") @RequestParam String city) {
        return ResponseEntity.ok(studentService.getStudentsByCity(city));
    }

    // =========================================================================
    // ── 2. JPA (JPQL) QUERY METHODS ──────────────────────────────────────────
    // =========================================================================

    @Operation(summary = "Find students by department name via JPQL (JPA Query)",
            description = "Uses custom JPQL query with JOIN: SELECT s FROM Student s JOIN s.department d WHERE ...")
    @ApiResponse(responseCode = "200", description = "Students retrieved via JPQL")
    @GetMapping("/jpql/by-department")
    public ResponseEntity<List<StudentDto>> getStudentsByDepartmentNameJpql(
            @Parameter(description = "Department name") @RequestParam String departmentName) {
        return ResponseEntity.ok(studentService.getStudentsByDepartmentNameJpql(departmentName));
    }

    @Operation(summary = "Filter students by course and city via JPQL (JPA Query)",
            description = "Uses custom JPQL query with multiple parameters: SELECT s FROM Student s WHERE ...")
    @ApiResponse(responseCode = "200", description = "Students filtered via JPQL")
    @GetMapping("/jpql/filter")
    public ResponseEntity<List<StudentDto>> getStudentsByCourseAndCityJpql(
            @Parameter(description = "Course name") @RequestParam String course,
            @Parameter(description = "City name") @RequestParam String city) {
        return ResponseEntity.ok(studentService.getStudentsByCourseAndCityJpql(course, city));
    }

    @Operation(summary = "Get student summaries via JPQL DTO projection (JPA Query)",
            description = "Uses JPQL constructor expression: SELECT new com.arpit.StudentManagementSystem.dto.StudentSummaryDto(...)")
    @ApiResponse(responseCode = "200", description = "Student summaries retrieved via JPQL projection")
    @GetMapping("/jpql/summaries")
    public ResponseEntity<List<StudentSummaryDto>> getStudentSummariesByCourseJpql(
            @Parameter(description = "Course name") @RequestParam String course) {
        return ResponseEntity.ok(studentService.getStudentSummariesByCourseJpql(course));
    }

    // =========================================================================
    // ── 3. NATIVE SQL QUERY METHODS ──────────────────────────────────────────
    // =========================================================================

    @Operation(summary = "Find students by email domain via Native SQL (Native Query)",
            description = "Uses native SQL: SELECT * FROM student WHERE email LIKE CONCAT('%', :domain)")
    @ApiResponse(responseCode = "200", description = "Students matching domain retrieved via native SQL")
    @GetMapping("/native/by-email-domain")
    public ResponseEntity<List<StudentDto>> getStudentsByEmailDomainNative(
            @Parameter(description = "Domain suffix (e.g. gmail.com)") @RequestParam String domain) {
        return ResponseEntity.ok(studentService.getStudentsByEmailDomainNative(domain));
    }

    @Operation(summary = "Find students by city via Native SQL JOIN (Native Query)",
            description = "Uses native SQL with INNER JOIN addresses: SELECT s.* FROM student s INNER JOIN addresses a ...")
    @ApiResponse(responseCode = "200", description = "Students in city retrieved via native SQL JOIN")
    @GetMapping("/native/by-city")
    public ResponseEntity<List<StudentDto>> getStudentsByCityNative(
            @Parameter(description = "City name") @RequestParam String city) {
        return ResponseEntity.ok(studentService.getStudentsByCityNative(city));
    }

    @Operation(summary = "Count students in department via Native SQL (Native Query)",
            description = "Uses native SQL aggregation: SELECT COUNT(*) FROM student WHERE department_id = :deptId")
    @ApiResponse(responseCode = "200", description = "Student count retrieved via native SQL")
    @GetMapping("/native/count-by-department/{departmentId}")
    public ResponseEntity<Long> getStudentCountByDepartmentNative(
            @Parameter(description = "Department ID") @PathVariable Long departmentId) {
        return ResponseEntity.ok(studentService.getStudentCountByDepartmentNative(departmentId));
    }

    // =========================================================================
    // ── 4. PROJECTION METHODS (Interface Projections) ────────────────────────
    // =========================================================================

    @Operation(summary = "Get student projections by course (Interface Projection via Derived Query)",
            description = "Returns Spring Data interface projection (closed getters + open SpEL getStudentDetails())")
    @ApiResponse(responseCode = "200", description = "Interface projections retrieved")
    @GetMapping("/projections/by-course")
    public ResponseEntity<List<StudentProjection>> getStudentProjectionsByCourse(
            @Parameter(description = "Course name") @RequestParam String course) {
        return ResponseEntity.ok(studentService.getStudentProjectionsByCourse(course));
    }

    @Operation(summary = "Get student projections by course (Interface Projection via Native Query)",
            description = "Returns Spring Data interface projection mapped from native SQL SELECT aliases")
    @ApiResponse(responseCode = "200", description = "Interface projections retrieved via native SQL")
    @GetMapping("/projections/native/by-course")
    public ResponseEntity<List<StudentProjection>> getStudentProjectionsByCourseNative(
            @Parameter(description = "Course name") @RequestParam String course) {
        return ResponseEntity.ok(studentService.getStudentProjectionsByCourseNative(course));
    }
}
