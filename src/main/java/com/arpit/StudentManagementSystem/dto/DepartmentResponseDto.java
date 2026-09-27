package com.arpit.StudentManagementSystem.dto;


import com.arpit.StudentManagementSystem.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentResponseDto {
    Long id;
    String name;
//    private List<StudentDto> students;
}
