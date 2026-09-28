package com.arpit.StudentManagementSystem.config;

import com.arpit.StudentManagementSystem.entity.Department;
import com.arpit.StudentManagementSystem.repository.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds initial Department data into the database on first startup.
 * Safe to run on every restart — skips seeding if departments already exist.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DepartmentRepository departmentRepository;

    public DataInitializer(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void run(String... args) {
        if (departmentRepository.count() > 0) {
            log.info("DataInitializer: Departments already exist ({} found). Skipping seed.",
                    departmentRepository.count());
            return;
        }

        log.info("DataInitializer: No departments found. Seeding initial data...");

        List<Department> departments = List.of(
                new Department(null, "Computer Science"),
                new Department(null, "Information Technology"),
                new Department(null, "Electronics & Communication"),
                new Department(null, "Mechanical Engineering"),
                new Department(null, "Civil Engineering"),
                new Department(null, "Electrical Engineering"),
                new Department(null, "Mathematics"),
                new Department(null, "Physics"),
                new Department(null, "Business Administration"),
                new Department(null, "Data Science & AI")
        );

        departmentRepository.saveAll(departments);
        log.info("DataInitializer: Successfully seeded {} departments.", departments.size());
    }
}
