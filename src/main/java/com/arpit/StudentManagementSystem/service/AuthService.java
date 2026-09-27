package com.arpit.StudentManagementSystem.service;

import com.arpit.StudentManagementSystem.dto.LoginRequestDto;
import com.arpit.StudentManagementSystem.dto.LoginResponseDto;
import com.arpit.StudentManagementSystem.dto.RegisterRequestDto;

public interface AuthService {

    String register(RegisterRequestDto registerRequestDto);

    LoginResponseDto login(LoginRequestDto loginRequestDto);
}
