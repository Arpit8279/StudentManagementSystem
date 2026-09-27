package com.arpit.StudentManagementSystem.service.impl;

import com.arpit.StudentManagementSystem.dto.*;
import com.arpit.StudentManagementSystem.entity.Address;
import com.arpit.StudentManagementSystem.entity.Department;
import com.arpit.StudentManagementSystem.entity.Student;
import com.arpit.StudentManagementSystem.exception.DuplicateEmailException;
import com.arpit.StudentManagementSystem.exception.StudentNotFoundException;
import com.arpit.StudentManagementSystem.repository.DepartmentRepository;
import com.arpit.StudentManagementSystem.repository.StudentRepository;
import com.arpit.StudentManagementSystem.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional          // class-level default: all methods are write-transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentServiceImpl(StudentRepository studentRepository,
                              DepartmentRepository departmentRepository,
                              PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ── Helper: Entity → StudentDto ───────────────────────────────────────────

    private StudentDto toStudentDto(Student student) {
        Address address = student.getAddress();
        AddressResponseDto addressDto = null;
        if (address != null) {
            addressDto = new AddressResponseDto(
                    address.getId(),
                    address.getCity(),
                    address.getState(),
                    address.getCountry());
        }

        Department department = student.getDepartment();
        DepartmentResponseDto departmentDto = null;
        if (department != null) {
            departmentDto = new DepartmentResponseDto(department.getId(), department.getName());
        }

        return new StudentDto(
                student.getId(),
                student.getCourse(),
                student.getName(),
                student.getEmail(),
                addressDto,
                departmentDto);
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Override
    // inherits class-level @Transactional (readOnly = false)
    public StudentDto createStudent(AddStudentRequestDto dto) {
        log.info("Creating student with email: {}", dto.getEmail());

        // 1. Check duplicate email
        if (studentRepository.existsByEmail(dto.getEmail())) {
            log.warn("Duplicate email detected: {}", dto.getEmail());
            throw new DuplicateEmailException("A student with email '" + dto.getEmail() + "' already exists.");
        }

        // 2. Find Department
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> {
                    log.error("Department not found with id: {}", dto.getDepartmentId());
                    return new IllegalArgumentException(
                            "Department not found with id: " + dto.getDepartmentId());
                });

        // 3. Build Address
        Address address = null;
        if (dto.getAddressRequestDto() != null) {
            address = new Address();
            address.setCity(dto.getAddressRequestDto().getCity());
            address.setState(dto.getAddressRequestDto().getState());
            address.setCountry(dto.getAddressRequestDto().getCountry());
        }

        // 4. Build and save Student
        //    Password is hashed via BCryptPasswordEncoder before persisting.
        Student student = new Student();
        student.setName(dto.getName());
        student.setEmail(dto.getEmail());
        if (dto.getPassword() != null) {
            student.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        student.setCourse(dto.getCourse());
        student.setAddress(address);
        student.setDepartment(department);
        student.setCreatedAt(LocalDateTime.now());

        Student savedStudent = studentRepository.save(student);
        log.info("Student created successfully with id: {}", savedStudent.getId());
        return toStudentDto(savedStudent);
    }

    // ── READ ALL ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)     // read-only: no dirty-checking, smaller TX overhead
    public List<StudentDto> getAllstudent() {
        log.debug("Fetching all students");
        List<StudentDto> students = studentRepository.findAll()
                .stream()
                .map(this::toStudentDto)
                .toList();
        log.debug("Found {} students", students.size());
        return students;
    }

    // ── READ BY ID ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public StudentDto getStudent(Long id) {
        log.debug("Fetching student with id: {}", id);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Student not found with id: {}", id);
                    return new StudentNotFoundException("Student not found with id: " + id);
                });
        return toStudentDto(student);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Override
    // inherits class-level @Transactional (readOnly = false)
    public void deleteStudentById(Long id) {
        log.info("Deleting student with id: {}", id);
        if (!studentRepository.existsById(id)) {
            log.warn("Delete failed — student not found with id: {}", id);
            throw new StudentNotFoundException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
        log.info("Student with id: {} deleted successfully", id);
    }

    // ── FULL UPDATE (PUT) ─────────────────────────────────────────────────────

    @Override
    // inherits class-level @Transactional (readOnly = false)
    public StudentDto updateStudent(Long id, AddStudentRequestDto dto) {
        log.info("Full update of student with id: {}", id);

        // 1. Find existing Student
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed — student not found with id: {}", id);
                    return new StudentNotFoundException("Student not found with id: " + id);
                });

        // 2. Update Student fields
        student.setName(dto.getName());
        student.setEmail(dto.getEmail());
        student.setCourse(dto.getCourse());

        // 3. Hash and update password if provided
        if (dto.getPassword() != null) {
            student.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // 4. Update Address (full replace)
        if (dto.getAddressRequestDto() != null) {
            Address address = student.getAddress();
            if (address == null) {
                address = new Address();
                student.setAddress(address);
            }
            address.setCity(dto.getAddressRequestDto().getCity());
            address.setState(dto.getAddressRequestDto().getState());
            address.setCountry(dto.getAddressRequestDto().getCountry());
        }

        // 5. Update Department if provided
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Department not found with id: " + dto.getDepartmentId()));
            student.setDepartment(department);
        }

        Student updatedStudent = studentRepository.save(student);
        log.info("Student with id: {} updated successfully", id);
        return toStudentDto(updatedStudent);
    }

    // ── PARTIAL UPDATE (PATCH) ────────────────────────────────────────────────

    @Override
    // inherits class-level @Transactional (readOnly = false)
    public StudentDto partialupdateStudent(Long id, AddStudentRequestDto dto) {
        log.info("Partial update of student with id: {}", id);

        // 1. Find existing Student
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Partial update failed — student not found with id: {}", id);
                    return new StudentNotFoundException("Student not found with id: " + id);
                });

        // 2. Update only non-null Student fields
        if (dto.getName() != null) student.setName(dto.getName());
        if (dto.getEmail() != null) student.setEmail(dto.getEmail());
        if (dto.getCourse() != null) student.setCourse(dto.getCourse());

        // 3. Hash and set password only if supplied
        if (dto.getPassword() != null) {
            student.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // 4. Update only non-null Address fields
        if (dto.getAddressRequestDto() != null) {
            Address address = student.getAddress();
            if (address == null) {
                address = new Address();
                student.setAddress(address);
            }
            AddressRequestDto addressDto = dto.getAddressRequestDto();
            if (addressDto.getCity() != null) address.setCity(addressDto.getCity());
            if (addressDto.getState() != null) address.setState(addressDto.getState());
            if (addressDto.getCountry() != null) address.setCountry(addressDto.getCountry());
        }

        // 5. Update Department if provided
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Department not found with id: " + dto.getDepartmentId()));
            student.setDepartment(department);
        }

        Student updatedStudent = studentRepository.save(student);
        log.info("Student with id: {} partially updated", id);
        return toStudentDto(updatedStudent);
    }

    // =========================================================================
    // 1. DERIVED QUERY METHODS
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<StudentDto> getStudentsByCourse(String course) {
        log.debug("Fetching students by course: {}", course);
        return studentRepository.findByCourseIgnoreCase(course)
                .stream()
                .map(this::toStudentDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDto> searchStudentsByName(String name) {
        log.debug("Searching students with name containing: {}", name);
        return studentRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::toStudentDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDto> getStudentsByDepartmentName(String departmentName) {
        log.debug("Fetching students by department name: {}", departmentName);
        return studentRepository.findByDepartmentNameIgnoreCase(departmentName)
                .stream()
                .map(this::toStudentDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDto> getStudentsByCity(String city) {
        log.debug("Fetching students by address city: {}", city);
        return studentRepository.findByAddressCityIgnoreCase(city)
                .stream()
                .map(this::toStudentDto)
                .toList();
    }

    // =========================================================================
    // 2. JPA (JPQL) QUERY METHODS
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<StudentDto> getStudentsByDepartmentNameJpql(String departmentName) {
        log.debug("Fetching students by department name via JPQL: {}", departmentName);
        return studentRepository.findStudentsByDepartmentNameJpql(departmentName)
                .stream()
                .map(this::toStudentDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDto> getStudentsByCourseAndCityJpql(String course, String city) {
        log.debug("Fetching students by course [{}] and city [{}] via JPQL", course, city);
        return studentRepository.findStudentsByCourseAndCityJpql(course, city)
                .stream()
                .map(this::toStudentDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentSummaryDto> getStudentSummariesByCourseJpql(String course) {
        log.debug("Fetching student DTO summaries by course [{}] via JPQL constructor projection", course);
        return studentRepository.findStudentSummariesByCourseJpql(course);
    }

    // =========================================================================
    // 3. NATIVE SQL QUERY METHODS
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<StudentDto> getStudentsByEmailDomainNative(String domain) {
        log.debug("Fetching students by email domain [{}] via native SQL", domain);
        return studentRepository.findStudentsByEmailDomainNative(domain)
                .stream()
                .map(this::toStudentDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDto> getStudentsByCityNative(String city) {
        log.debug("Fetching students by city [{}] via native SQL INNER JOIN", city);
        return studentRepository.findStudentsByCityNative(city)
                .stream()
                .map(this::toStudentDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getStudentCountByDepartmentNative(Long departmentId) {
        log.debug("Counting students in department [{}] via native SQL aggregate", departmentId);
        return studentRepository.countStudentsByDepartmentNative(departmentId);
    }

    // =========================================================================
    // 4. PROJECTION METHODS
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<StudentProjection> getStudentProjectionsByCourse(String course) {
        log.debug("Fetching interface projections by course [{}] via derived query", course);
        return studentRepository.findProjectionsByCourseIgnoreCase(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentProjection> getStudentProjectionsByCourseNative(String course) {
        log.debug("Fetching interface projections by course [{}] via native SQL query", course);
        return studentRepository.findStudentProjectionsByCourseNative(course);
    }
}
