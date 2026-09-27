package com.arpit.StudentManagementSystem.controller;

import com.arpit.StudentManagementSystem.dto.AddressResponseDto;
import com.arpit.StudentManagementSystem.dto.DepartmentResponseDto;
import com.arpit.StudentManagementSystem.dto.StudentDto;
import com.arpit.StudentManagementSystem.dto.StudentProjection;
import com.arpit.StudentManagementSystem.dto.StudentSummaryDto;
import com.arpit.StudentManagementSystem.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentController studentController;

    private StudentDto studentDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(studentController).build();

        studentDto = new StudentDto(
                1L,
                "B.Tech",
                "Arpit Sahu",
                "arpit@test.com",
                new AddressResponseDto(1L, "Bangalore", "Karnataka", "India"),
                new DepartmentResponseDto(1L, "Computer Science")
        );
    }

    // ── 1. Derived Query Endpoints ───────────────────────────────────────────

    @Test
    @DisplayName("GET /search/by-course: returns 200 with students")
    void getStudentsByCourse_returnsOk() throws Exception {
        when(studentService.getStudentsByCourse("B.Tech")).thenReturn(List.of(studentDto));

        mockMvc.perform(get("/api/v1/students/search/by-course")
                        .param("course", "B.Tech")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Arpit Sahu"))
                .andExpect(jsonPath("$[0].course").value("B.Tech"));

        verify(studentService).getStudentsByCourse("B.Tech");
    }

    @Test
    @DisplayName("GET /search/by-name: returns 200 with matching students")
    void searchStudentsByName_returnsOk() throws Exception {
        when(studentService.searchStudentsByName("Arpit")).thenReturn(List.of(studentDto));

        mockMvc.perform(get("/api/v1/students/search/by-name")
                        .param("name", "Arpit")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Arpit Sahu"));

        verify(studentService).searchStudentsByName("Arpit");
    }

    @Test
    @DisplayName("GET /search/by-department: returns 200 with matching students")
    void getStudentsByDepartment_returnsOk() throws Exception {
        when(studentService.getStudentsByDepartmentName("Computer Science")).thenReturn(List.of(studentDto));

        mockMvc.perform(get("/api/v1/students/search/by-department")
                        .param("departmentName", "Computer Science")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department.name").value("Computer Science"));

        verify(studentService).getStudentsByDepartmentName("Computer Science");
    }

    @Test
    @DisplayName("GET /search/by-city: returns 200 with matching students")
    void getStudentsByCity_returnsOk() throws Exception {
        when(studentService.getStudentsByCity("Bangalore")).thenReturn(List.of(studentDto));

        mockMvc.perform(get("/api/v1/students/search/by-city")
                        .param("city", "Bangalore")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].address.city").value("Bangalore"));

        verify(studentService).getStudentsByCity("Bangalore");
    }

    // ── 2. JPA (JPQL) Query Endpoints ────────────────────────────────────────

    @Test
    @DisplayName("GET /jpql/by-department: returns 200 via JPQL")
    void getStudentsByDepartmentNameJpql_returnsOk() throws Exception {
        when(studentService.getStudentsByDepartmentNameJpql("Computer Science")).thenReturn(List.of(studentDto));

        mockMvc.perform(get("/api/v1/students/jpql/by-department")
                        .param("departmentName", "Computer Science")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Arpit Sahu"));

        verify(studentService).getStudentsByDepartmentNameJpql("Computer Science");
    }

    @Test
    @DisplayName("GET /jpql/filter: returns 200 via JPQL with course & city")
    void getStudentsByCourseAndCityJpql_returnsOk() throws Exception {
        when(studentService.getStudentsByCourseAndCityJpql("B.Tech", "Bangalore")).thenReturn(List.of(studentDto));

        mockMvc.perform(get("/api/v1/students/jpql/filter")
                        .param("course", "B.Tech")
                        .param("city", "Bangalore")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].course").value("B.Tech"));

        verify(studentService).getStudentsByCourseAndCityJpql("B.Tech", "Bangalore");
    }

    @Test
    @DisplayName("GET /jpql/summaries: returns 200 with DTO projections")
    void getStudentSummariesByCourseJpql_returnsOk() throws Exception {
        StudentSummaryDto summary = new StudentSummaryDto(1L, "Arpit Sahu", "arpit@test.com", "B.Tech", "Computer Science");
        when(studentService.getStudentSummariesByCourseJpql("B.Tech")).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/students/jpql/summaries")
                        .param("course", "B.Tech")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].departmentName").value("Computer Science"))
                .andExpect(jsonPath("$[0].course").value("B.Tech"));

        verify(studentService).getStudentSummariesByCourseJpql("B.Tech");
    }

    // ── 3. Native Query Endpoints ────────────────────────────────────────────

    @Test
    @DisplayName("GET /native/by-email-domain: returns 200 with matching students")
    void getStudentsByEmailDomainNative_returnsOk() throws Exception {
        when(studentService.getStudentsByEmailDomainNative("test.com")).thenReturn(List.of(studentDto));

        mockMvc.perform(get("/api/v1/students/native/by-email-domain")
                        .param("domain", "test.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("arpit@test.com"));

        verify(studentService).getStudentsByEmailDomainNative("test.com");
    }

    @Test
    @DisplayName("GET /native/by-city: returns 200 with joined students")
    void getStudentsByCityNative_returnsOk() throws Exception {
        when(studentService.getStudentsByCityNative("Bangalore")).thenReturn(List.of(studentDto));

        mockMvc.perform(get("/api/v1/students/native/by-city")
                        .param("city", "Bangalore")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Arpit Sahu"));

        verify(studentService).getStudentsByCityNative("Bangalore");
    }

    @Test
    @DisplayName("GET /native/count-by-department/{id}: returns 200 with count")
    void getStudentCountByDepartmentNative_returnsOk() throws Exception {
        when(studentService.getStudentCountByDepartmentNative(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/v1/students/native/count-by-department/{departmentId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(studentService).getStudentCountByDepartmentNative(1L);
    }

    // ── 4. Projection Endpoints ──────────────────────────────────────────────

    private static class TestStudentProjection implements StudentProjection {
        private final Long id;
        private final String name;
        private final String email;
        private final String course;

        public TestStudentProjection(Long id, String name, String email, String course) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.course = course;
        }

        @Override public Long getId() { return id; }
        @Override public String getName() { return name; }
        @Override public String getEmail() { return email; }
        @Override public String getCourse() { return course; }
        @Override public String getStudentDetails() { return name + " (" + course + ")"; }
    }

    @Test
    @DisplayName("GET /projections/by-course: returns 200 with interface projections")
    void getStudentProjectionsByCourse_returnsOk() throws Exception {
        StudentProjection projection = new TestStudentProjection(1L, "Arpit Sahu", "arpit@test.com", "B.Tech");

        when(studentService.getStudentProjectionsByCourse("B.Tech")).thenReturn(List.of(projection));

        mockMvc.perform(get("/api/v1/students/projections/by-course")
                        .param("course", "B.Tech")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Arpit Sahu"))
                .andExpect(jsonPath("$[0].studentDetails").value("Arpit Sahu (B.Tech)"));

        verify(studentService).getStudentProjectionsByCourse("B.Tech");
    }

    @Test
    @DisplayName("GET /projections/native/by-course: returns 200 with native interface projections")
    void getStudentProjectionsByCourseNative_returnsOk() throws Exception {
        StudentProjection projection = new TestStudentProjection(1L, "Arpit Sahu", "arpit@test.com", "B.Tech");

        when(studentService.getStudentProjectionsByCourseNative("B.Tech")).thenReturn(List.of(projection));

        mockMvc.perform(get("/api/v1/students/projections/native/by-course")
                        .param("course", "B.Tech")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Arpit Sahu"));

        verify(studentService).getStudentProjectionsByCourseNative("B.Tech");
    }
}
