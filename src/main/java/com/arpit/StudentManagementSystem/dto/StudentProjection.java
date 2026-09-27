package com.arpit.StudentManagementSystem.dto;

import org.springframework.beans.factory.annotation.Value;

/**
 * Interface-based projection for Student entity.
 * Demonstrates both:
 * 1. Closed projection: direct getter access to specific fields (optimized SQL query).
 * 2. Open projection: computed value via SpEL expression accessing target properties.
 */
public interface StudentProjection {

    // ── Closed Projection Getters ─────────────────────────────────────────────
    Long getId();

    String getName();

    String getEmail();

    String getCourse();

    // ── Open Projection (SpEL expression) ─────────────────────────────────────
    @Value("#{target.name + ' (' + target.course + ')'}")
    String getStudentDetails();
}
