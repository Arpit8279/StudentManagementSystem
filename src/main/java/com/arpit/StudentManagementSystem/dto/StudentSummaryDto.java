package com.arpit.StudentManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class-based DTO projection for Student.
 * Used with JPQL constructor expressions:
 * SELECT new com.arpit.StudentManagementSystem.dto.StudentSummaryDto(...) FROM Student s
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentSummaryDto {
    private Long id;
    private String name;
    private String email;
    private String course;
    private String departmentName;
}
