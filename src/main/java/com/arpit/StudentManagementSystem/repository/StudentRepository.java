package com.arpit.StudentManagementSystem.repository;

import com.arpit.StudentManagementSystem.dto.StudentProjection;
import com.arpit.StudentManagementSystem.dto.StudentSummaryDto;
import com.arpit.StudentManagementSystem.entity.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByEmail(String email);

    // =========================================================================
    // STANDARD CRUD OVERRIDES with @EntityGraph (prevents N+1 on address/dept)
    // =========================================================================

    /**
     * Override findAll() with an EntityGraph so that address and department
     * associations are fetched in a single JOIN query instead of N+1 selects.
     */
    @Override
    @EntityGraph(attributePaths = {"address", "department"})
    List<Student> findAll();

    /**
     * Override findById() with an EntityGraph so associations are eager in one query.
     */
    @Override
    @EntityGraph(attributePaths = {"address", "department"})
    Optional<Student> findById(Long id);

    // =========================================================================
    // 1. DERIVED QUERY METHODS (Spring Data method name resolution)
    // =========================================================================

    /**
     * Find students by course (case-insensitive).
     * @EntityGraph ensures address and department are joined in the same query.
     * Generated query: SELECT s FROM Student s WHERE UPPER(s.course) = UPPER(?1)
     */
    @EntityGraph(attributePaths = {"address", "department"})
    List<Student> findByCourseIgnoreCase(String course);

    /**
     * Find students whose name contains the given string (LIKE %name%, case-insensitive).
     * @EntityGraph ensures no lazy-load round-trips when mapping to StudentDto.
     */
    @EntityGraph(attributePaths = {"address", "department"})
    List<Student> findByNameContainingIgnoreCase(String name);

    /**
     * Find students by department name traversing nested entity (department.name).
     * Generated query: SELECT s FROM Student s LEFT JOIN s.department d WHERE UPPER(d.name) = UPPER(?1)
     */
    @EntityGraph(attributePaths = {"address", "department"})
    List<Student> findByDepartmentNameIgnoreCase(String departmentName);

    /**
     * Find students by address city traversing nested entity (address.city).
     * Generated query: SELECT s FROM Student s LEFT JOIN s.address a WHERE UPPER(a.city) = UPPER(?1)
     */
    @EntityGraph(attributePaths = {"address", "department"})
    List<Student> findByAddressCityIgnoreCase(String city);

    /**
     * Find students matching both course and department id.
     */
    @EntityGraph(attributePaths = {"address", "department"})
    List<Student> findByCourseIgnoreCaseAndDepartmentId(String course, Long departmentId);

    // =========================================================================
    // 2. JPA (JPQL) QUERIES (@Query)
    // =========================================================================

    /**
     * Custom JPQL query joining Student with Department entity.
     * Joins address and department eagerly via LEFT JOIN FETCH to prevent N+1.
     */
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.address LEFT JOIN FETCH s.department d WHERE LOWER(d.name) = LOWER(:deptName)")
    List<Student> findStudentsByDepartmentNameJpql(@Param("deptName") String deptName);

    /**
     * Custom JPQL query filtering by course and associated address city.
     * Uses LEFT JOIN FETCH to eagerly load associations.
     */
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.address LEFT JOIN FETCH s.department WHERE LOWER(s.course) = LOWER(:course) AND LOWER(s.address.city) = LOWER(:city)")
    List<Student> findStudentsByCourseAndCityJpql(@Param("course") String course, @Param("city") String city);

    /**
     * Custom JPQL DTO / Class-based projection using constructor expression.
     * Only fetches the needed columns — no entity graph needed here.
     */
    @Query("SELECT new com.arpit.StudentManagementSystem.dto.StudentSummaryDto(" +
           "s.id, s.name, s.email, s.course, s.department.name) " +
           "FROM Student s WHERE LOWER(s.course) = LOWER(:course)")
    List<StudentSummaryDto> findStudentSummariesByCourseJpql(@Param("course") String course);

    // =========================================================================
    // 3. NATIVE SQL QUERIES (@Query with nativeQuery = true)
    // =========================================================================

    /**
     * Native SQL query matching email domain suffix directly on the database table.
     */
    @Query(value = "SELECT * FROM student WHERE email LIKE CONCAT('%', :domain)", nativeQuery = true)
    List<Student> findStudentsByEmailDomainNative(@Param("domain") String domain);

    /**
     * Native SQL query performing an explicit INNER JOIN with the addresses table.
     */
    @Query(value = "SELECT s.* FROM student s INNER JOIN addresses a ON s.address_id = a.id WHERE LOWER(a.city) = LOWER(:city)", nativeQuery = true)
    List<Student> findStudentsByCityNative(@Param("city") String city);

    /**
     * Native SQL query returning an aggregate count of students enrolled in a department.
     */
    @Query(value = "SELECT COUNT(*) FROM student WHERE department_id = :deptId", nativeQuery = true)
    Long countStudentsByDepartmentNative(@Param("deptId") Long deptId);

    // =========================================================================
    // 4. PROJECTIONS (Interface & Dynamic Projections)
    // =========================================================================

    /**
     * Interface-based projection via derived query method.
     * Spring Data JPA creates a proxy implementing StudentProjection.
     * No @EntityGraph needed — projection only fetches declared columns.
     */
    List<StudentProjection> findProjectionsByCourseIgnoreCase(String course);

    /**
     * Interface-based projection via native SQL query.
     * Column aliases must match projection property names (id, name, email, course).
     */
    @Query(value = "SELECT s.id AS id, s.name AS name, s.email AS email, s.course AS course " +
                   "FROM student s WHERE LOWER(s.course) = LOWER(:course)", nativeQuery = true)
    List<StudentProjection> findStudentProjectionsByCourseNative(@Param("course") String course);

    /**
     * Dynamic projection query: target type is specified at invocation time.
     */
    <T> List<T> findByCourseIgnoreCase(String course, Class<T> type);
}
