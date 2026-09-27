# Student Management System

A production-ready **RESTful API** built with Spring Boot for managing student records. Features JWT-based authentication, role-based access control, full CRUD operations, and a comprehensive showcase of Spring Data JPA query techniques.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Features](#features)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [JPA Query Techniques](#jpa-query-techniques)
- [Exception Handling](#exception-handling)
- [Running Tests](#running-tests)
- [Environment Variables](#environment-variables)

---

## Overview

This project demonstrates a complete Spring Boot backend with:

- Secure JWT authentication with BCrypt password hashing
- Role-based authorization (`ADMIN` / `STUDENT`)
- Full CRUD for Students, Departments, and Addresses
- Four Spring Data JPA query strategies implemented end-to-end
- N+1 query prevention using `@EntityGraph`
- Proper transaction management with `@Transactional`
- Centralized exception handling with structured error responses
- Interactive API documentation via Swagger UI

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 19 |
| Framework | Spring Boot 4.1.0 |
| Security | Spring Security 7 + JJWT 0.12.6 |
| Persistence | Spring Data JPA + Hibernate 7 |
| Database | MySQL 8 (Aiven Cloud) |
| API Docs | SpringDoc OpenAPI 3 / Swagger UI |
| Build Tool | Maven (with Maven Wrapper) |
| Testing | JUnit 5 + Mockito + MockMvc |
| Utilities | Lombok, Jakarta Validation |

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                        HTTP Request                              │
└─────────────────────────┬────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│              JwtAuthFilter (Spring Security Filter Chain)         │
│  Validates Bearer token, sets SecurityContext on every request   │
└─────────────────────────┬────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│                     Controller Layer                              │
│     StudentController  │  AuthController  │  DepartmentController│
│         (REST endpoints, request validation, Swagger docs)        │
└─────────────────────────┬────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│                      Service Layer                                │
│    StudentService  │  AuthService  │  DepartmentService          │
│      (@Transactional boundaries, business logic, DTO mapping)     │
└─────────────────────────┬────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│                    Repository Layer                               │
│   StudentRepository  │  UserRepository  │  DepartmentRepository  │
│  (Spring Data JPA — derived / JPQL / native / projections)       │
└─────────────────────────┬────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│                MySQL 8 Database (Aiven Cloud)                     │
│        Student  │  Department  │  Address  │  User               │
└──────────────────────────────────────────────────────────────────┘
```

---

## Features

### Security
- **JWT Authentication** — stateless token-based auth with configurable expiry
- **BCrypt Password Hashing** — passwords never stored in plain text
- **Role-based Authorization** — `ADMIN` and `STUDENT` roles with method-level security
- **`@JsonIgnore` on password** — never exposed in any API response

### Data Layer
- **`@EntityGraph`** on all repository fetch methods — eliminates N+1 queries on `address` and `department` associations
- **`@Transactional(readOnly = true)`** on all read operations — Hibernate skips dirty-checking, smaller transaction overhead
- **`@Transactional`** on all write operations — full rollback on exception

### API
- **Full CRUD** — Create, Read, Update (PUT + PATCH), Delete for Students and Departments
- **Bean Validation** — `@NotBlank`, `@Email`, `@Size`, `@NotNull` on all request DTOs
- **Structured Error Responses** — consistent JSON body with timestamp, status, message, path, and per-field validation errors
- **Swagger UI** — interactive documentation at `/swagger-ui.html`

### JPA Query Showcase
Four query strategies all demonstrated end-to-end (Repository → Service → Controller → Tests):
1. **Derived Query Methods** — Spring Data method name resolution
2. **JPQL Queries** — `@Query` with `LEFT JOIN FETCH`
3. **Native SQL Queries** — `@Query(nativeQuery = true)` with JOINs and aggregates
4. **Projections** — Interface-based and Class-based (DTO constructor expressions)

---

## Project Structure

```
src/
├── main/
│   ├── java/com/arpit/StudentManagementSystem/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java          # JWT filter chain, BCrypt bean, CORS
│   │   │   ├── OpenApiConfig.java           # Swagger / SpringDoc config
│   │   │   └── RequestLoggingFilter.java    # HTTP request/response logging
│   │   │
│   │   ├── controller/
│   │   │   ├── AuthController.java          # POST /auth/register, /auth/login
│   │   │   ├── StudentController.java       # Full CRUD + all query endpoints
│   │   │   └── DepartmentController.java    # Department CRUD
│   │   │
│   │   ├── dto/
│   │   │   ├── AddStudentRequestDto.java    # Create/update student request
│   │   │   ├── RegisterRequestDto.java      # Auth registration request
│   │   │   ├── LoginRequestDto.java         # Auth login request
│   │   │   ├── LoginResponseDto.java        # JWT token response
│   │   │   ├── StudentDto.java              # Student response DTO
│   │   │   ├── StudentProjection.java       # Interface-based projection
│   │   │   ├── StudentSummaryDto.java       # Class-based DTO projection
│   │   │   └── Address/DepartmentDtos.java  # Address & department DTOs
│   │   │
│   │   ├── entity/
│   │   │   ├── Student.java                 # @Entity — @JsonIgnore on password
│   │   │   ├── Department.java              # @Entity — @OneToMany Students
│   │   │   ├── Address.java                 # @Entity — @OneToOne with Student
│   │   │   ├── User.java                    # @Entity — login credentials + role
│   │   │   └── Role.java                    # Enum: ADMIN, STUDENT
│   │   │
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java  # @RestControllerAdvice — 400/401/403/404/409/500
│   │   │   ├── ErrorResponse.java           # Structured error body
│   │   │   ├── StudentNotFoundException.java
│   │   │   └── DuplicateEmailException.java
│   │   │
│   │   ├── repository/
│   │   │   ├── StudentRepository.java       # @EntityGraph + all 4 query types
│   │   │   ├── DepartmentRepository.java
│   │   │   └── UserRepository.java
│   │   │
│   │   ├── security/
│   │   │   ├── JwtAuthFilter.java           # OncePerRequestFilter — validates JWT
│   │   │   ├── JwtUtil.java                 # Token generation & validation (JJWT)
│   │   │   ├── CustomUserDetailsService.java
│   │   │   └── CustomUserDetail.java
│   │   │
│   │   └── service/
│   │       ├── StudentService.java          # Interface
│   │       ├── AuthService.java             # Interface
│   │       ├── DepartmentService.java       # Interface
│   │       └── impl/
│   │           ├── StudentServiceImpl.java  # @Transactional service layer
│   │           ├── AuthServiceImpl.java     # Register + login logic
│   │           └── DepartmentServiceImpl.java
│   │
│   └── resources/
│       ├── application.properties           # Public config (env var placeholders)
│       └── static/                          # HTML frontend (index, dashboard, register)
│
└── test/
    └── java/com/arpit/StudentManagementSystem/
        ├── service/impl/
        │   ├── StudentServiceImplTest.java  # Unit tests — Mockito (@Nested suites)
        │   └── AuthServiceImplTest.java     # Auth unit tests
        └── controller/
            └── StudentControllerTest.java   # Web layer tests — MockMvc
```

---

## Getting Started

### Prerequisites

- Java 19+
- Maven 3.9+ (or use the included `./mvnw` wrapper)
- MySQL 8 instance (local or cloud — see [Aiven free tier](https://aiven.io))

### 1. Clone the Repository

```bash
git clone https://github.com/Arpit8279/StudentManagementSystem.git
cd StudentManagementSystem
```

### 2. Create the Database

```sql
CREATE DATABASE defaultdb;
```

> If using Aiven, the database `defaultdb` is created automatically.

### 3. Configure Local Credentials

Create `src/main/resources/application-local.properties` — this file is **git-ignored** and must not be committed:

```properties
# Local MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/defaultdb
spring.datasource.username=root
spring.datasource.password=your_password

# OR — Aiven Cloud MySQL
# spring.datasource.url=jdbc:mysql://<host>:<port>/defaultdb?sslMode=REQUIRED
# spring.datasource.username=avnadmin
# spring.datasource.password=your_aiven_password

jwt.secret=your-very-long-secret-minimum-32-characters
jwt.expiration-ms=86400000
```

### 4. Run the Application

```bash
# Using Maven Wrapper (no Maven installation needed)
./mvnw spring-boot:run --spring.config.additional-location=src/main/resources/application-local.properties

# OR set environment variables and run
export DB_URL=jdbc:mysql://localhost:3306/defaultdb
export DB_USERNAME=root
export DB_PASSWORD=your_password
export JWT_SECRET=your-very-long-secret
./mvnw spring-boot:run
```

### 5. Access the API

| URL | Description |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Interactive API docs (Swagger UI) |
| `http://localhost:8080/api/v1/auth/register` | Register endpoint |
| `http://localhost:8080/api/v1/auth/login` | Login endpoint |
| `http://localhost:8080/api/v1/students` | Students CRUD |

---

## API Reference

### Authentication — `POST /api/v1/auth` (Public, no token required)

#### Register

```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "arpit@example.com",
  "password": "secret123",
  "name": "Arpit Sahu",
  "course": "B.Tech",
  "departmentId": 1,
  "addressRequestDto": {
    "city": "Bangalore",
    "state": "Karnataka",
    "country": "India"
  }
}
```

**Response `201 Created`:**
```
Student registered successfully with email: arpit@example.com
```

#### Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "arpit@example.com",
  "password": "secret123"
}
```

**Response `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "arpit@example.com",
  "role": "STUDENT"
}
```

Use the token in all subsequent requests:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

### Students — `GET|POST|PUT|PATCH|DELETE /api/v1/students`

| Method | Path | Role | Description |
|---|---|---|---|
| `POST` | `/api/v1/students` | `ADMIN` | Create a student |
| `GET` | `/api/v1/students` | Any | Get all students |
| `GET` | `/api/v1/students/{id}` | Any | Get student by ID |
| `PUT` | `/api/v1/students/{id}` | `ADMIN` / `STUDENT` | Full update |
| `PATCH` | `/api/v1/students/{id}` | `ADMIN` / `STUDENT` | Partial update |
| `DELETE` | `/api/v1/students/{id}` | `ADMIN` | Delete student |

### Departments — `GET|POST|PUT|DELETE /api/v1/departments`

| Method | Path | Role | Description |
|---|---|---|---|
| `POST` | `/api/v1/departments` | `ADMIN` | Create department |
| `GET` | `/api/v1/departments` | Any | Get all departments |
| `GET` | `/api/v1/departments/{id}` | Any | Get department by ID |
| `PUT` | `/api/v1/departments/{id}` | `ADMIN` | Update department |
| `DELETE` | `/api/v1/departments/{id}` | `ADMIN` | Delete department |

---

## JPA Query Techniques

All four strategies are implemented end-to-end: **Repository → Service → Controller → Tests**

### 1. Derived Query Methods

Spring Data resolves method names into SQL at startup — no `@Query` annotation needed.

| Endpoint | Method | Description |
|---|---|---|
| `GET /students/search/by-course?course=B.Tech` | `findByCourseIgnoreCase` | Case-insensitive course filter |
| `GET /students/search/by-name?name=arpit` | `findByNameContainingIgnoreCase` | LIKE search on name |
| `GET /students/search/by-department?name=CS` | `findByDepartmentNameIgnoreCase` | Traverse nested entity |
| `GET /students/search/by-city?city=Bangalore` | `findByAddressCityIgnoreCase` | Traverse nested entity |

### 2. JPQL Queries (`@Query`)

Custom HQL/JPQL with explicit `LEFT JOIN FETCH` to eagerly load associations.

| Endpoint | Description |
|---|---|
| `GET /students/jpql/by-department?name=CS` | JPQL JOIN on department name |
| `GET /students/jpql/filter?course=B.Tech&city=Bangalore` | Multi-param JPQL filter |
| `GET /students/jpql/summaries?course=B.Tech` | DTO constructor projection (only needed columns) |

### 3. Native SQL Queries (`@Query(nativeQuery = true)`)

Raw SQL for maximum control — JOINs, aggregates, non-JPA-expressible queries.

| Endpoint | Description |
|---|---|
| `GET /students/native/by-email-domain?domain=@gmail.com` | SQL LIKE on email suffix |
| `GET /students/native/by-city?city=Bangalore` | SQL INNER JOIN with addresses table |
| `GET /students/native/count-by-department/{id}` | SQL `COUNT(*)` aggregate |

### 4. Projections

Fetch only required columns — avoids loading full entity graphs when not needed.

| Endpoint | Type | Description |
|---|---|---|
| `GET /students/projections/by-course?course=B.Tech` | Interface projection (derived) | Only id, name, email, course |
| `GET /students/projections/native/by-course?course=B.Tech` | Interface projection (native SQL) | Aliased columns mapped to interface |

---

## Exception Handling

All exceptions are handled by `GlobalExceptionHandler` (`@RestControllerAdvice`) and return a consistent JSON body:

```json
{
  "timestamp": "2026-09-27T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Student not found with id: 99",
  "path": "/api/v1/students/99"
}
```

Validation errors include a per-field `errors` map:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed — check the 'errors' field for details",
  "errors": {
    "email": "Email must be a valid email address",
    "password": "Password must be at least 8 characters"
  }
}
```

| Exception | HTTP Status |
|---|---|
| `StudentNotFoundException` | `404 Not Found` |
| `DuplicateEmailException` | `409 Conflict` |
| `MethodArgumentNotValidException` | `400 Bad Request` |
| `IllegalArgumentException` | `400 Bad Request` |
| `BadCredentialsException` | `401 Unauthorized` |
| `AuthorizationDeniedException` | `403 Forbidden` |
| `Exception` (catch-all) | `500 Internal Server Error` |

---

## Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=StudentServiceImplTest
./mvnw test -Dtest=StudentControllerTest
./mvnw test -Dtest=AuthServiceImplTest
```

**Test coverage summary:**

| Test Class | Type | Tests | Description |
|---|---|---|---|
| `StudentServiceImplTest` | Unit (Mockito) | 22 | Service logic with `@Nested` suites per query category |
| `StudentControllerTest` | Web Layer (MockMvc) | 12 | HTTP status, JSON response, security |
| `AuthServiceImplTest` | Unit (Mockito) | 5 | Register, login, duplicate email |

All **39 tests pass** with zero failures.

---

## Environment Variables

The application reads secrets via environment variables with safe fallback defaults in `application.properties`. **Never commit real credentials.**

| Variable | Description | Example |
|---|---|---|
| `DB_URL` | JDBC connection URL | `jdbc:mysql://localhost:3306/defaultdb` |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | `your_password` |
| `JWT_SECRET` | JWT signing key (min 32 chars) | `a-very-long-secret-key-...` |
| `JWT_EXPIRATION_MS` | Token expiry in milliseconds | `86400000` (24h) |

For local development, override via `src/main/resources/application-local.properties` (git-ignored).

---

## License

This project is open source and available under the [MIT License](LICENSE).
