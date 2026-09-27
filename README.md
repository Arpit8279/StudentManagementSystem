# 🎓 Student Management System

A production-ready **Spring Boot REST API** for managing student records with JWT-based authentication, role-based access control, and comprehensive JPA query coverage.

---

## 🚀 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 19 |
| Framework | Spring Boot 4.1.0 |
| Security | Spring Security + JWT (JJWT 0.12.6) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8 |
| API Docs | SpringDoc / Swagger UI |
| Build | Maven |
| Testing | JUnit 5 + Mockito + MockMvc |

---

## ✨ Features

- **JWT Authentication** — Register and login with BCrypt password hashing
- **Role-based Access Control** — `ADMIN` and `STUDENT` roles
- **Full CRUD** on Students, Departments, and Addresses
- **4 Query Strategies** demonstrated end-to-end:
  - Derived Query Methods (`findByCourseIgnoreCase`, `findByNameContaining...`)
  - JPQL Queries (`@Query` with entity joins and `LEFT JOIN FETCH`)
  - Native SQL Queries (`@Query(nativeQuery = true)` with aggregates and JOINs)
  - Interface & Class-based Projections (`StudentProjection`, `StudentSummaryDto`)
- **N+1 Prevention** via `@EntityGraph` on all repository fetch methods
- **Transaction Management** — `@Transactional(readOnly = true)` on reads, write TX on mutations
- **Swagger UI** at `/swagger-ui.html`

---

## 🏗️ Project Structure

```
src/
├── main/java/com/arpit/StudentManagementSystem/
│   ├── config/         # SecurityConfig
│   ├── controller/     # StudentController, DepartmentController
│   ├── dto/            # Request/Response DTOs, Projections
│   ├── entity/         # Student, Department, Address, User, Role
│   ├── exception/      # Custom exceptions + global handler
│   ├── repository/     # JPA repositories
│   ├── security/       # JWT filter, UserDetailsService
│   └── service/        # Service interfaces + implementations
└── test/               # Unit tests (Mockito) + Web layer tests (MockMvc)
```

---

## ⚙️ Local Setup

### Prerequisites
- Java 19+
- MySQL 8
- Maven 3.8+

### 1. Clone the repo
```bash
git clone https://github.com/<your-username>/StudentManagementSystem.git
cd StudentManagementSystem/StudentManagementSystem
```

### 2. Create the database
```sql
CREATE DATABASE studentsdb;
```

### 3. Configure local credentials

Create `src/main/resources/application-local.properties` (this file is git-ignored):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/studentsdb
spring.datasource.username=root
spring.datasource.password=your_password

jwt.secret=your-very-long-secret-key-minimum-32-chars
jwt.expiration-ms=86400000
```

### 4. Run the application
```bash
# With the local profile (picks up application-local.properties automatically)
./mvnw spring-boot:run
```

Or set environment variables directly:
```bash
set DB_PASSWORD=your_password
set JWT_SECRET=your_secret
./mvnw spring-boot:run
```

### 5. Access Swagger UI
Open: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 🔐 API Authentication Flow

```
POST /api/v1/auth/register   →  Creates student + user account
POST /api/v1/auth/login      →  Returns JWT token
```

Add the token as a Bearer header on all protected endpoints:
```
Authorization: Bearer <your_jwt_token>
```

---

## 📡 Key Endpoints

### Students
| Method | Path | Role | Description |
|---|---|---|---|
| `POST` | `/api/v1/students` | ADMIN | Create student |
| `GET` | `/api/v1/students` | Authenticated | Get all students |
| `GET` | `/api/v1/students/{id}` | Authenticated | Get by ID |
| `PUT` | `/api/v1/students/{id}` | ADMIN / STUDENT | Full update |
| `PATCH` | `/api/v1/students/{id}` | ADMIN / STUDENT | Partial update |
| `DELETE` | `/api/v1/students/{id}` | ADMIN | Delete |

### Query Demos
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/students/search/by-course` | Derived query |
| `GET` | `/api/v1/students/search/by-name` | LIKE derived query |
| `GET` | `/api/v1/students/search/by-department` | Association traversal |
| `GET` | `/api/v1/students/search/by-city` | Association traversal |
| `GET` | `/api/v1/students/jpql/by-department` | Custom JPQL JOIN |
| `GET` | `/api/v1/students/jpql/filter` | Multi-param JPQL |
| `GET` | `/api/v1/students/jpql/summaries` | JPQL DTO projection |
| `GET` | `/api/v1/students/native/by-email-domain` | Native SQL |
| `GET` | `/api/v1/students/native/by-city` | Native SQL JOIN |
| `GET` | `/api/v1/students/native/count-by-department/{id}` | Native SQL COUNT |
| `GET` | `/api/v1/students/projections/by-course` | Interface projection |
| `GET` | `/api/v1/students/projections/native/by-course` | Native projection |

---

## 🧪 Running Tests

```bash
./mvnw test
```

All 40 unit and web layer tests pass.

---

## 📝 License

MIT
