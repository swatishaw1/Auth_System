# Multi-Provider Authentication & Authorization System

A production-oriented authentication and authorization backend built with **Spring Boot 4**, **Spring Security**, **JWT**, **OAuth2**, and **MySQL**.

The system supports traditional email/password authentication as well as **Google and GitHub OAuth2 login**, with secure access-token and refresh-token handling, refresh-token rotation, role-based authorization, OTP-based password recovery, email verification, pagination, validation, and centralized exception handling.

---

## Features

### Authentication

* User registration
* Email/password login
* JWT access-token authentication
* JWT refresh-token mechanism
* Refresh-token rotation
* Secure logout with token revocation
* Access and refresh tokens stored using cookies
* HttpOnly cookie support
* Configurable cookie security settings

### OAuth2 Login

* Google OAuth2 authentication
* GitHub OAuth2 authentication
* Custom OAuth2 user information handling
* Provider-aware authentication
* OAuth2 success and failure handlers
* Automatic user creation/update for OAuth2 users

### Password Recovery

* Email-based password recovery
* OTP generation
* OTP delivery through email
* OTP expiration
* OTP verification
* Password reset after successful OTP verification

### Authorization and Security

* Spring Security
* Role-based authorization
* JWT request filtering
* Secure password hashing using BCrypt
* Disabled-account protection
* Refresh-token revocation
* Refresh-token rotation
* Stateless authentication

### User Management

* Create users
* Retrieve users
* Retrieve users by email
* Update users
* Delete users
* Pagination
* Sorting
* DTO-based API responses

### API and Backend Quality

* RESTful APIs
* Jakarta Bean Validation
* Global exception handling
* Structured API error responses
* DTO layer to prevent direct entity exposure
* Service-layer business logic
* Repository abstraction using Spring Data JPA
* Swagger/OpenAPI documentation
* MySQL persistence

---

## Tech Stack

| Technology         | Purpose                          |
| ------------------ | -------------------------------- |
| Java 21            | Programming language             |
| Spring Boot 4.0.5  | Backend framework                |
| Spring Security    | Authentication and authorization |
| Spring Data JPA    | Database access                  |
| Hibernate          | ORM                              |
| MySQL              | Relational database              |
| JJWT 0.13.0        | JWT creation and validation      |
| OAuth2 Client      | Google and GitHub authentication |
| Spring Mail        | OTP and password recovery emails |
| Jakarta Validation | Request validation               |
| ModelMapper        | DTO and entity mapping           |
| Lombok             | Boilerplate reduction            |
| SpringDoc OpenAPI  | API documentation                |
| Maven              | Build and dependency management  |

---

## Architecture

The project follows a layered backend architecture:

```text
Client
   |
   v
REST Controllers
   |
   v
Service Layer
   |
   v
Repository Layer
   |
   v
MySQL Database
```

Security-related requests additionally pass through the JWT authentication filter:

```text
HTTP Request
     |
     v
JwtAuthenticationFilter
     |
     v
JWT Validation
     |
     v
Spring Security Context
     |
     v
Controller
```

---

## Project Structure

```text
authBackend/
|
├── src/
│   ├── main/
│   │   ├── java/com/example/authBackend/
│   │   │
│   │   ├── Enum/
│   │   │   ├── Provider.java
│   │   │   └── Role.java
│   │   │
│   │   ├── api/
│   │   │   ├── MailBody.java
│   │   │   ├── request/
│   │   │   │   ├── ForgotPasswordRequest.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── RefreshTokenRequest.java
│   │   │   │   ├── ResetPasswordRequest.java
│   │   │   │   └── VerifyOtpRequest.java
│   │   │   │
│   │   │   └── response/
│   │   │       ├── ApiErrorResponse.java
│   │   │       ├── ErrorResponse.java
│   │   │       ├── TokenResponse.java
│   │   │       └── UserResponse.java
│   │   │
│   │   ├── configuration/
│   │   │   ├── ApiDocConfig.java
│   │   │   ├── AppConfig.java
│   │   │   ├── AppConstants.java
│   │   │   └── SecurityConfig.java
│   │   │
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   └── UserController.java
│   │   │
│   │   ├── dto/
│   │   │   ├── RoleDTO.java
│   │   │   └── UserDTO.java
│   │   │
│   │   ├── exceptions/
│   │   │   ├── GlobalExceptionHandling.java
│   │   │   └── ResourceNotFoundException.java
│   │   │
│   │   ├── filter/
│   │   │   └── JwtAuthenticationFilter.java
│   │   │
│   │   ├── helper/
│   │   │   └── UserHelper.java
│   │   │
│   │   ├── model/
│   │   │   ├── ForgetPassword.java
│   │   │   ├── RefreshToken.java
│   │   │   └── User.java
│   │   │
│   │   ├── repository/
│   │   │   ├── ForgetPasswordRepository.java
│   │   │   ├── RefreshTokenRepository.java
│   │   │   └── UserRepository.java
│   │   │
│   │   ├── security/
│   │   │   ├── CookieService.java
│   │   │   └── oauth2/
│   │   │       ├── OAuth2SuccessHandler.java
│   │   │       ├── Oauth2FailureHandler.java
│   │   │       └── userInfo/
│   │   │           ├── CustomOAuth2UserService.java
│   │   │           ├── CustomeUserDetailsService.java
│   │   │           ├── GitHubOAuth2UserInfo.java
│   │   │           ├── GoogleOAuth2UserInfo.java
│   │   │           ├── OAuth2UserInfo.java
│   │   │           └── Oauth2UserInfoFactory.java
│   │   │
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── AuthenticationService.java
│   │   │   ├── EmailService.java
│   │   │   ├── RefreshTokenService.java
│   │   │   ├── UserService.java
│   │   │   └── implementation/
│   │   │       ├── AuthServiceImpl.java
│   │   │       ├── AuthenticationServiceImpl.java
│   │   │       ├── EmailServiceImpl.java
│   │   │       ├── RefreshTokenServiceImpl.java
│   │   │       └── UserServiceImpl.java
│   │   │
│   │   └── utils/
│   │       └── JwtService.java
│   │
│   └── resources/
│       ├── application.yaml
│       └── application-dev.yml
│
├── Auth System.postman_collection.json
├── pom.xml
├── mvnw
└── mvnw.cmd
```

---

## Authentication Flow

### Local Registration

```text
Client
  |
  | POST /api/v1/auth/register
  v
AuthController
  |
  v
AuthService
  |
  v
UserService
  |
  v
UserRepository
  |
  v
MySQL
```

The user's password is encoded using **BCrypt** before being stored.

### Login

```text
User
 |
 | Email + Password
 v
POST /api/v1/auth/login
 |
 v
AuthenticationService
 |
 v
Spring Security
 |
 v
Password Verification
 |
 v
JWT Access Token
+
JWT Refresh Token
 |
 v
Secure Cookies
```

The system generates:

* Access token
* Refresh token
* Unique refresh-token identifier (`jti`)

The refresh token is stored and tracked server-side so that it can later be revoked or rotated.

---

## Refresh Token Rotation

When the access token expires, the client can request a new token pair.

```text
Old Refresh Token
       |
       v
Validate Token
       |
       v
Generate New JTI
       |
       v
Generate New Access Token
       +
Generate New Refresh Token
       |
       v
Revoke/Replace Old Token
       |
       v
Return New Token Pair
```

Refresh-token rotation reduces the security risk associated with long-lived refresh tokens.

---

## Logout

Logout performs more than simply deleting a client-side token.

```text
Logout Request
      |
      v
Read Refresh Token
      |
      v
Validate JWT
      |
      v
Extract JTI
      |
      v
Revoke Refresh Token
      |
      v
Clear Authentication Cookies
      |
      v
Clear Security Context
```

---

## Password Reset Flow

The password recovery process uses email OTP verification.

```text
1. User submits email
          |
          v
2. Email is verified
          |
          v
3. OTP generated
          |
          v
4. OTP sent through email
          |
          v
5. User submits OTP
          |
          v
6. OTP validated and expiration checked
          |
          v
7. User submits new password
          |
          v
8. Password is BCrypt encoded
          |
          v
9. Password updated
```

### Password Recovery Endpoints

```text
POST /api/v1/auth/verifyEmail
POST /api/v1/auth/verifyOtp
POST /api/v1/auth/resetPassword
```

---

## OAuth2 Authentication

The application supports:

* Google
* GitHub

OAuth2 users are handled through custom provider-specific classes.

```text
User
 |
 v
Google / GitHub
 |
 v
OAuth2 Callback
 |
 v
CustomOAuth2UserService
 |
 v
Provider-specific UserInfo
 |
 v
User Creation / Update
 |
 v
OAuth2 Success Handler
 |
 v
Application
```

Provider information is represented using an enum:

```java
Provider.LOCAL
Provider.GOOGLE
Provider.GITHUB
```

This separates authentication behavior based on the user's authentication provider.

---

## User Roles

The application supports role-based authorization.

Roles are represented using an enum and included in the authenticated user's security authorities.

Example:

```text
USER
ADMIN
```

The available roles are defined in:

```text
Enum/Role.java
```

---

## Pagination and Sorting

User listing supports pagination and sorting.

Example:

```http
GET /api/v1/users?pageNumber=0&pageSize=10&sortBy=name&sortOrder=asc
```

Spring Data's `Pageable` mechanism is used internally.

Pagination helps:

* Avoid loading every user at once
* Reduce database load
* Improve API performance
* Scale better for larger datasets

---

## API Endpoints

### Authentication APIs

| Method | Endpoint                     | Description                        |
| ------ | ---------------------------- | ---------------------------------- |
| POST   | `/api/v1/auth/register`      | Register a new user                |
| POST   | `/api/v1/auth/login`         | Login with email/password          |
| POST   | `/api/v1/auth/refresh`       | Generate new access/refresh tokens |
| POST   | `/api/v1/auth/logout`        | Logout and revoke refresh token    |
| POST   | `/api/v1/auth/verifyEmail`   | Send password-reset OTP            |
| POST   | `/api/v1/auth/verifyOtp`     | Verify OTP                         |
| POST   | `/api/v1/auth/resetPassword` | Reset password                     |

### User APIs

| Method | Endpoint                        | Description         |
| ------ | ------------------------------- | ------------------- |
| GET    | `/api/v1/users`                 | Get paginated users |
| POST   | `/api/v1/users`                 | Create a user       |
| PUT    | `/api/v1/users/{userId}`        | Update a user       |
| DELETE | `/api/v1/users/{userId}`        | Delete a user       |
| GET    | `/api/v1/users/byEmail/{email}` | Get user by email   |

---

## JWT Design

The application uses two types of JWT:

### Access Token

The access token contains information such as:

```text
User ID
Email
Roles
Issuer
Issued Time
Expiration Time
Token Type = access
```

### Refresh Token

The refresh token contains:

```text
User ID
Issuer
Issued Time
Expiration Time
JTI
Token Type = refresh
```

The JWT algorithm used by the application is:

```text
HS512
```

The JWT secret must be at least **64 characters** long.

---

## Cookie Security

Authentication tokens can be attached to cookies with configurable properties:

```yaml
security:
  jwt:
    refresh-token-cookie-name: ${JWT_REFRESH_TOKEN_COOKIE_NAME}
    access-token-cookie-name: ${JWT_ACCESS_TOKEN_COOKIE_NAME}
    cookie-secure: ${JWT_COOKIE_SECURE}
    cookie-http-only: ${JWT_COOKIE_HTTP_ONLY}
    cookie-same-site: ${JWT_COOKIE_SAME_SITE}
    cookie-domain: ${JWT_COOKIE_DOMAIN}
```

This allows cookie behavior to be configured separately for development and production environments.

---

## Database

The application uses MySQL for persistence.

```text
MySQL
   |
   ├── User
   ├── RefreshToken
   └── ForgetPassword
```

Hibernate/JPA handles database persistence.

The development configuration uses:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

For production environments, database migration tools such as **Flyway** or **Liquibase** would be preferable to relying on `ddl-auto: update`.

---

## Configuration

The application uses environment variables for sensitive configuration instead of hardcoding credentials.

Required configuration includes:

```text
MYSQL_URL
MYSQL_ROOT_USERNAME
MYSQL_ROOT_PASSWORD

JWT_SECRET
JWT_ISSUER
JWT_ACCESS_TTL_SECONDS
JWT_REFRESH_TTL_SECONDS

JWT_REFRESH_TOKEN_COOKIE_NAME
JWT_ACCESS_TOKEN_COOKIE_NAME
JWT_COOKIE_SECURE
JWT_COOKIE_HTTP_ONLY
JWT_COOKIE_SAME_SITE
JWT_COOKIE_DOMAIN

SMTP_USERNAME
SMTP_PASSWORD
OTP_VALIDATION_TIME

GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET

GITHUB_CLIENT_ID
GITHUB_CLIENT_SECRET
```

Never commit sensitive values such as:

```text
JWT_SECRET
MYSQL_ROOT_PASSWORD
SMTP_PASSWORD
GOOGLE_CLIENT_SECRET
GITHUB_CLIENT_SECRET
```

to a public repository.

---

## Running the Project Locally

### Prerequisites

Install:

* Java 21
* Maven
* MySQL
* Git

### 1. Clone the Repository

```bash
git clone https://github.com/swatishaw1/Auth_System.git
```

### 2. Enter the Backend Directory

```bash
cd Auth_System/authBackend
```

### 3. Configure Environment Variables

Set the required environment variables listed in the configuration section.

### 4. Create the MySQL Database

Create a MySQL database and configure the following environment variables:

```text
MYSQL_URL
MYSQL_ROOT_USERNAME
MYSQL_ROOT_PASSWORD
```

### 5. Run the Application

Using Maven:

```bash
mvn spring-boot:run
```

Or using the Maven wrapper.

#### Windows

```bash
mvnw.cmd spring-boot:run
```

#### Linux/macOS

```bash
./mvnw spring-boot:run
```

The application runs by default on:

```text
http://localhost:8080
```

---

## API Documentation

The project includes **SpringDoc OpenAPI** support.

After starting the application, Swagger UI can be accessed at:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger can be used to:

* Explore available endpoints
* View request and response models
* Test APIs
* Understand API contracts

---

## Postman

A Postman collection is included in the project:

```text
Auth System.postman_collection.json
```

Import the collection into Postman to test the authentication and user-management APIs.

### Authentication Testing Flow

```text
1. Register
      |
      v
2. Login
      |
      v
3. Access Protected API
      |
      v
4. Refresh Token
      |
      v
5. Logout
```

### Password Recovery Testing Flow

```text
1. verifyEmail
      |
      v
2. Check Email
      |
      v
3. verifyOtp
      |
      v
4. resetPassword
```

---

## Security Practices Implemented

* BCrypt password hashing
* JWT signature validation
* Separate access and refresh token types
* Refresh-token rotation
* Refresh-token revocation
* HttpOnly cookie configuration
* Configurable Secure and SameSite cookie attributes
* OAuth2 provider validation
* Account enabled checks
* Role-based authorities
* No-store headers for authentication responses
* Environment-based secret configuration
* Global exception handling
* Request validation

---

## Design Decisions

### DTOs Instead of Exposing Entities

The API uses DTOs such as:

```text
UserDTO
RoleDTO
TokenResponse
UserResponse
```

This reduces direct coupling between the database model and API contract.

### Service Layer

Business logic is separated from controllers:

```text
Controller
    |
    v
Service
    |
    v
Repository
```

This keeps controllers focused on handling HTTP requests while business logic remains inside the service layer.

### Provider Enum

Authentication providers are explicitly represented:

```text
LOCAL
GOOGLE
GITHUB
```

This provides a clear way to distinguish authentication methods and makes the authentication architecture easier to extend.

### Refresh Token Persistence

Refresh tokens are tracked using persistent refresh-token records.

This allows the application to:

* Rotate tokens
* Revoke tokens
* Track token identifiers
* Invalidate sessions during logout

---

## Future Improvements

The current implementation can be further strengthened by adding:

* Flyway or Liquibase database migrations
* Automated integration tests
* Unit tests for authentication services
* Rate limiting for login and OTP endpoints
* Account lockout after repeated failed logins
* OTP attempt limits
* Email verification during registration
* Redis-based token/session management
* CSRF strategy documentation for cookie-based authentication
* Production Docker configuration
* CI/CD pipeline
* Production monitoring and logging
* More granular role and permission management

---

## What This Project Demonstrates

This project demonstrates practical backend concepts beyond basic CRUD:

```text
Spring Boot
     |
Spring Security
     |
JWT Authentication
     |
Refresh Token Rotation
     |
OAuth2
     |
Role-Based Authorization
     |
OTP Password Recovery
     |
Email Integration
     |
MySQL / JPA
     |
Pagination
     |
Validation
     |
Exception Handling
     |
REST API Design
```

The project provides a foundation for applications that require multiple authentication providers, secure token handling, user management, and a layered backend architecture.

---

## Author

**Swati**

GitHub:
https://github.com/swatishaw1
