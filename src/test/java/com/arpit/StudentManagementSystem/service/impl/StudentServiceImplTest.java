package com.arpit.StudentManagementSystem.service.impl;

import com.arpit.StudentManagementSystem.dto.*;
import com.arpit.StudentManagementSystem.entity.Address;
import com.arpit.StudentManagementSystem.entity.Department;
import com.arpit.StudentManagementSystem.entity.Student;
import com.arpit.StudentManagementSystem.exception.DuplicateEmailException;
import com.arpit.StudentManagementSystem.exception.StudentNotFoundException;
import com.arpit.StudentManagementSystem.repository.DepartmentRepository;
import com.arpit.StudentManagementSystem.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Department department;
    private Address address;
    private Student student;

    @BeforeEach
    void setUp() {
        department = new Department(1L, "Computer Science");
        address = new Address(1L, "Bangalore", "Karnataka", "India");
        student = new Student(1L, "Arpit Sahu", "arpit@test.com", null, "B.Tech",
                LocalDateTime.now(), address, department);
    }

    // ── createStudent ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createStudent: success — returns StudentDto")
    void createStudent_success() {
        AddressRequestDto addressReq = new AddressRequestDto("Bangalore", "Karnataka", "India");
        AddStudentRequestDto dto = new AddStudentRequestDto(
                "Arpit Sahu", "arpit@test.com", null, "B.Tech", addressReq, 1L);

        when(studentRepository.existsByEmail("arpit@test.com")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        StudentDto result = studentService.createStudent(dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Arpit Sahu");
        assertThat(result.getEmail()).isEqualTo("arpit@test.com");
        assertThat(result.getDepartment().getName()).isEqualTo("Computer Science");
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    @DisplayName("createStudent: throws DuplicateEmailException when email exists")
    void createStudent_duplicateEmail_throwsException() {
        AddStudentRequestDto dto = new AddStudentRequestDto(
                "Arpit", "arpit@test.com", null, "B.Tech", null, 1L);
        when(studentRepository.existsByEmail("arpit@test.com")).thenReturn(true);

        assertThatThrownBy(() -> studentService.createStudent(dto))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("arpit@test.com");

        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("createStudent: throws IllegalArgumentException when department not found")
    void createStudent_departmentNotFound_throwsException() {
        AddStudentRequestDto dto = new AddStudentRequestDto(
                "Arpit", "arpit@test.com", null, "B.Tech", null, 999L);
        when(studentRepository.existsByEmail("arpit@test.com")).thenReturn(false);
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.createStudent(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("999");
    }

    // ── getAllstudent ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllstudent: returns list of StudentDto")
    void getAllstudent_returnsStudentList() {
        when(studentRepository.findAll()).thenReturn(List.of(student));

        List<StudentDto> result = studentService.getAllstudent();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Arpit Sahu");
        assertThat(result.get(0).getDepartment()).isNotNull();
        assertThat(result.get(0).getAddress()).isNotNull();
    }

    @Test
    @DisplayName("getAllstudent: returns empty list when no students")
    void getAllstudent_emptyList() {
        when(studentRepository.findAll()).thenReturn(List.of());
        assertThat(studentService.getAllstudent()).isEmpty();
    }

    // ── getStudent ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getStudent: success — returns StudentDto")
    void getStudent_success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        StudentDto result = studentService.getStudent(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("arpit@test.com");
    }

    @Test
    @DisplayName("getStudent: throws StudentNotFoundException when not found")
    void getStudent_notFound_throwsException() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudent(99L))
                .isInstanceOf(StudentNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── deleteStudentById ─────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteStudentById: success")
    void deleteStudentById_success() {
        when(studentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(studentRepository).deleteById(1L);

        studentService.deleteStudentById(1L);

        verify(studentRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteStudentById: throws StudentNotFoundException when not found")
    void deleteStudentById_notFound_throwsException() {
        when(studentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> studentService.deleteStudentById(99L))
                .isInstanceOf(StudentNotFoundException.class);

        verify(studentRepository, never()).deleteById(any());
    }

    // ── updateStudent ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateStudent: success — updates all fields")
    void updateStudent_success() {
        AddressRequestDto addressReq = new AddressRequestDto("Mumbai", "Maharashtra", "India");
        AddStudentRequestDto dto = new AddStudentRequestDto(
                "Arpit Updated", "arpit@test.com", null, "M.Tech", addressReq, 1L);

        Student updatedStudent = new Student(1L, "Arpit Updated", "arpit@test.com", null,
                "M.Tech", LocalDateTime.now(),
                new Address(1L, "Mumbai", "Maharashtra", "India"), department);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(studentRepository.save(any(Student.class))).thenReturn(updatedStudent);

        StudentDto result = studentService.updateStudent(1L, dto);

        assertThat(result.getName()).isEqualTo("Arpit Updated");
        assertThat(result.getCourse()).isEqualTo("M.Tech");
    }

    // =========================================================================
    // ── 1. DERIVED QUERY METHODS TESTS ───────────────────────────────────────
    // =========================================================================

    @Nested
    @DisplayName("Derived Query Methods")
    class DerivedQueriesTests {

        @Test
        @DisplayName("getStudentsByCourse: returns matching students")
        void getStudentsByCourse_success() {
            when(studentRepository.findByCourseIgnoreCase("B.Tech")).thenReturn(List.of(student));

            List<StudentDto> results = studentService.getStudentsByCourse("B.Tech");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getCourse()).isEqualTo("B.Tech");
            verify(studentRepository).findByCourseIgnoreCase("B.Tech");
        }

        @Test
        @DisplayName("searchStudentsByName: returns students matching name keyword")
        void searchStudentsByName_success() {
            when(studentRepository.findByNameContainingIgnoreCase("Arpit")).thenReturn(List.of(student));

            List<StudentDto> results = studentService.searchStudentsByName("Arpit");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getName()).isEqualTo("Arpit Sahu");
            verify(studentRepository).findByNameContainingIgnoreCase("Arpit");
        }

        @Test
        @DisplayName("getStudentsByDepartmentName: returns students in given department")
        void getStudentsByDepartmentName_success() {
            when(studentRepository.findByDepartmentNameIgnoreCase("Computer Science")).thenReturn(List.of(student));

            List<StudentDto> results = studentService.getStudentsByDepartmentName("Computer Science");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getDepartment().getName()).isEqualTo("Computer Science");
            verify(studentRepository).findByDepartmentNameIgnoreCase("Computer Science");
        }

        @Test
        @DisplayName("getStudentsByCity: returns students in given city")
        void getStudentsByCity_success() {
            when(studentRepository.findByAddressCityIgnoreCase("Bangalore")).thenReturn(List.of(student));

            List<StudentDto> results = studentService.getStudentsByCity("Bangalore");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getAddress().getCity()).isEqualTo("Bangalore");
            verify(studentRepository).findByAddressCityIgnoreCase("Bangalore");
        }
    }

    // =========================================================================
    // ── 2. JPA (JPQL) QUERY METHODS TESTS ────────────────────────────────────
    // =========================================================================

    @Nested
    @DisplayName("JPA (JPQL) Queries")
    class JpqlQueriesTests {

        @Test
        @DisplayName("getStudentsByDepartmentNameJpql: returns students from JPQL join")
        void getStudentsByDepartmentNameJpql_success() {
            when(studentRepository.findStudentsByDepartmentNameJpql("Computer Science")).thenReturn(List.of(student));

            List<StudentDto> results = studentService.getStudentsByDepartmentNameJpql("Computer Science");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getName()).isEqualTo("Arpit Sahu");
            verify(studentRepository).findStudentsByDepartmentNameJpql("Computer Science");
        }

        @Test
        @DisplayName("getStudentsByCourseAndCityJpql: returns filtered students")
        void getStudentsByCourseAndCityJpql_success() {
            when(studentRepository.findStudentsByCourseAndCityJpql("B.Tech", "Bangalore")).thenReturn(List.of(student));

            List<StudentDto> results = studentService.getStudentsByCourseAndCityJpql("B.Tech", "Bangalore");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getCourse()).isEqualTo("B.Tech");
            verify(studentRepository).findStudentsByCourseAndCityJpql("B.Tech", "Bangalore");
        }

        @Test
        @DisplayName("getStudentSummariesByCourseJpql: returns DTO projection summaries")
        void getStudentSummariesByCourseJpql_success() {
            StudentSummaryDto summaryDto = new StudentSummaryDto(1L, "Arpit Sahu", "arpit@test.com", "B.Tech", "Computer Science");
            when(studentRepository.findStudentSummariesByCourseJpql("B.Tech")).thenReturn(List.of(summaryDto));

            List<StudentSummaryDto> results = studentService.getStudentSummariesByCourseJpql("B.Tech");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(1L);
            assertThat(results.get(0).getName()).isEqualTo("Arpit Sahu");
            assertThat(results.get(0).getDepartmentName()).isEqualTo("Computer Science");
            verify(studentRepository).findStudentSummariesByCourseJpql("B.Tech");
        }
    }

    // =========================================================================
    // ── 3. NATIVE SQL QUERY METHODS TESTS ────────────────────────────────────
    // =========================================================================

    @Nested
    @DisplayName("Native SQL Queries")
    class NativeQueriesTests {

        @Test
        @DisplayName("getStudentsByEmailDomainNative: returns students matching email domain")
        void getStudentsByEmailDomainNative_success() {
            when(studentRepository.findStudentsByEmailDomainNative("test.com")).thenReturn(List.of(student));

            List<StudentDto> results = studentService.getStudentsByEmailDomainNative("test.com");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getEmail()).isEqualTo("arpit@test.com");
            verify(studentRepository).findStudentsByEmailDomainNative("test.com");
        }

        @Test
        @DisplayName("getStudentsByCityNative: returns students joined with address")
        void getStudentsByCityNative_success() {
            when(studentRepository.findStudentsByCityNative("Bangalore")).thenReturn(List.of(student));

            List<StudentDto> results = studentService.getStudentsByCityNative("Bangalore");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getAddress().getCity()).isEqualTo("Bangalore");
            verify(studentRepository).findStudentsByCityNative("Bangalore");
        }

        @Test
        @DisplayName("getStudentCountByDepartmentNative: returns student count aggregate")
        void getStudentCountByDepartmentNative_success() {
            when(studentRepository.countStudentsByDepartmentNative(1L)).thenReturn(5L);

            Long count = studentService.getStudentCountByDepartmentNative(1L);

            assertThat(count).isEqualTo(5L);
            verify(studentRepository).countStudentsByDepartmentNative(1L);
        }
    }

    // =========================================================================
    // ── 4. PROJECTION METHODS TESTS ──────────────────────────────────────────
    // =========================================================================

    @Nested
    @DisplayName("Projection Methods")
    class ProjectionTests {

        @Test
        @DisplayName("getStudentProjectionsByCourse: returns interface projections via derived query")
        void getStudentProjectionsByCourse_success() {
            StudentProjection projection = mock(StudentProjection.class);
            when(projection.getId()).thenReturn(1L);
            when(projection.getName()).thenReturn("Arpit Sahu");
            when(projection.getEmail()).thenReturn("arpit@test.com");
            when(projection.getCourse()).thenReturn("B.Tech");
            when(projection.getStudentDetails()).thenReturn("Arpit Sahu (B.Tech)");

            when(studentRepository.findProjectionsByCourseIgnoreCase("B.Tech")).thenReturn(List.of(projection));

            List<StudentProjection> results = studentService.getStudentProjectionsByCourse("B.Tech");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(1L);
            assertThat(results.get(0).getName()).isEqualTo("Arpit Sahu");
            assertThat(results.get(0).getEmail()).isEqualTo("arpit@test.com");
            assertThat(results.get(0).getCourse()).isEqualTo("B.Tech");
            assertThat(results.get(0).getStudentDetails()).isEqualTo("Arpit Sahu (B.Tech)");
            verify(studentRepository).findProjectionsByCourseIgnoreCase("B.Tech");
        }

        @Test
        @DisplayName("getStudentProjectionsByCourseNative: returns interface projections via native SQL")
        void getStudentProjectionsByCourseNative_success() {
            StudentProjection projection = mock(StudentProjection.class);
            when(projection.getId()).thenReturn(1L);
            when(projection.getName()).thenReturn("Arpit Sahu");

            when(studentRepository.findStudentProjectionsByCourseNative("B.Tech")).thenReturn(List.of(projection));

            List<StudentProjection> results = studentService.getStudentProjectionsByCourseNative("B.Tech");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(1L);
            assertThat(results.get(0).getName()).isEqualTo("Arpit Sahu");
            verify(studentRepository).findStudentProjectionsByCourseNative("B.Tech");
        }
    }
}
