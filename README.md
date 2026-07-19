# Multitenant Auth System (Spring Boot)

A production-style authentication and authorization backend built using **Spring Boot**, implementing secure login, role-based access, and extensible architecture.

A secure authentication system designed to support multiple tenants within a single application. Each tenant has isolated users, roles and data while sharing the same backend infrastructure. The system uses JWT-based authentication, role-based access control,and refresh token rotation to ensure secure and scalable user management across tenants.

---

## Features

- User Registration & Login
- JWT-based Authentication
- Secure Password Handling(BCrypt)
- Refresh Token Mechanism
- Exception Handling (Global)
- Validation (Jakarta Validation)
- **Advanced: Pagination using Pageable**
- Clean Layered Architecture

---

## This Project Does

Most beginner auth systems stop at login/register.
This project goes further:

- Handles **real-world security flows**
- Uses **Spring Security properly**
- Introduces **pagination (Pageable)** — for scalable APIs

---

## Tech Stack

- **Backend:** Spring Boot
- **Security:** Spring Security + JWT
- **Database:** JPA / Hibernate
- **Validation:** Jakarta Validation
- **Build Tool:** Maven / Gradle (depending on setup)

---

## Project Structure:

```
authBackend/
├── src/main/java/com/example/authBackend/
│   ├── Config/                 # App Configurations
│   ├── Controller/             # REST Controllers
│   ├── DTOs/                   # DTOs
│   │   └── Record/
│   ├── Exceptions/             # Global Exception Handling
│   ├── FilterSpecification/    # Specification For Filters
│   ├── Helper/
│   ├── Model/                  # JPA Entities
│   ├      └── enum/            # Enums (Role, Provider, etc.)
│   ├── Payload/                # Responses
│   ├── Repository/             # JPA Repositories
│   ├── Security/               # JWT, Filters, UserDetails
│   ├── Service/                # Business Logic Layer
│   │   └── Impl/               # Implementation of Business Logic
│   └── resources/              # Configurations
```

---

## Authentication Flow (Simple Breakdown)

1. User Registration
2. User logs in → credentials validated
3. Server generates **JWT token**
4. Token sent in headers for future requests
5. Spring Security filter validates token
6. Access granted based on roles

---

## Advanced Concept: Pageable (Pagination)

Instead of returning **all records**, this project uses:

```java
Pageable pageable = PageRequest.of(pageNumber,pageSize,sortByAndOrder);
Page<User> pageUser = userRepository.findAll(pageable);
```

### Why it standout:

- Prevents performance issues
- Supports scalable APIs
- Industry standard for large datasets

## Sample API Endpoints

| Module          | Method | Endpoint                                | Description                      |
| --------------- | ------ | --------------------------------------- | -------------------------------- |
| Auth            | POST   | /api/v1/auth/register                   | Register new user                |
| Auth            | POST   | /api/v1/auth/login                      | Login user                       |
| Auth            | POST   | /api/v1/auth/refresh                    | Refresh JWT token                |
| Auth            | POST   | /api/v1/auth/logout                     | Logout user                      |
|                 |        |                                         |                                  |
| User            | POST   | /api/v1/users                           | Create user                      |
| User            | GET    | /api/v1/users                           | Get all users (paginated)        |
| User            | GET    | /api/v1/users/byId/{id}                 | Get user by ID                   |
| User            | GET    | /api/v1/users/byEmail/{email}           | Get user by email                |
| User            | PUT    | /api/v1/users/{id}                      | Update user                      |
| User            | DELETE | /api/v1/users/{id}                      | Delete user                      |

## Key Design Decisions

- **DTO Layer** → prevents entity exposure
- **Service Layer** → keeps logic clean
- **Security Layer Isolation** → scalable auth logic
- **Enum-based Providers** → extensible

---

---

## How to Run

```bash
git clone https://github.com/swatishaw1/Auth_System.git
cd auth-system
mvn spring-boot:run
```
