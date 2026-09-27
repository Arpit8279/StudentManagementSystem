package com.arpit.StudentManagementSystem.service.impl;

import com.arpit.StudentManagementSystem.dto.AddressRequestDto;
import com.arpit.StudentManagementSystem.dto.LoginRequestDto;
import com.arpit.StudentManagementSystem.dto.LoginResponseDto;
import com.arpit.StudentManagementSystem.dto.RegisterRequestDto;
import com.arpit.StudentManagementSystem.entity.Address;
import com.arpit.StudentManagementSystem.entity.Department;
import com.arpit.StudentManagementSystem.entity.Role;
import com.arpit.StudentManagementSystem.entity.Student;
import com.arpit.StudentManagementSystem.entity.User;
import com.arpit.StudentManagementSystem.exception.DuplicateEmailException;
import com.arpit.StudentManagementSystem.repository.DepartmentRepository;
import com.arpit.StudentManagementSystem.repository.StudentRepository;
import com.arpit.StudentManagementSystem.repository.UserRepository;
import com.arpit.StudentManagementSystem.security.JwtUtil;
import com.arpit.StudentManagementSystem.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository,
                           StudentRepository studentRepository,
                           DepartmentRepository departmentRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public String register(RegisterRequestDto dto) {
        log.info("Registering new student with email: {}", dto.getEmail());

        // 1. Check duplicate email in User table
        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Registration failed — email already registered: {}", dto.getEmail());
            throw new DuplicateEmailException("Email already registered: " + dto.getEmail());
        }

        // 2. Build Address
        AddressRequestDto addressDto = dto.getAddressRequestDto();
        Address address = new Address();
        if (addressDto != null) {
            address.setCity(addressDto.getCity());
            address.setState(addressDto.getState());
            address.setCountry(addressDto.getCountry());
        }

        // 3. Find Department
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> {
                    log.error("Registration failed — department not found with id: {}", dto.getDepartmentId());
                    return new IllegalArgumentException(
                            "Department not found with id: " + dto.getDepartmentId());
                });

        // 4. Build and save Student
        Student student = new Student();
        student.setName(dto.getName());
        student.setEmail(dto.getEmail());
        student.setCourse(dto.getCourse());
        student.setAddress(address);
        student.setDepartment(department);
        student.setCreatedAt(LocalDateTime.now());
        Student savedStudent = studentRepository.save(student);

        // 5. Build and save User (linked to Student, password hashed)
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.STUDENT);
        user.setStudent(savedStudent);
        userRepository.save(user);

        log.info("Student registered successfully — email: {}, studentId: {}", dto.getEmail(), savedStudent.getId());
        return "Student registered successfully with email: " + dto.getEmail();
    }

    @Override
    public LoginResponseDto login(LoginRequestDto dto) {
        log.info("Login attempt for email: {}", dto.getEmail());

        // Authenticate credentials — throws AuthenticationException on failure
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Load the User entity to get the role
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtUtil.generateToken(userDetails.getUsername(), user.getRole().name());
        log.info("Login successful for email: {}, role: {}", dto.getEmail(), user.getRole());

        return new LoginResponseDto(token, user.getEmail(), user.getRole().name());
    }
}
