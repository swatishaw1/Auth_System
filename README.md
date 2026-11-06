# Full Stack Authentication & Authorization System

A full-stack authentication application built to explore practical
backend security, token management, OAuth2, rate limiting, automated
testing, CI, and containerization.

**Backend:** Java 21, Spring Boot 4.0.5, Spring Security\
**Frontend:** React, TypeScript, Vite\
**Database:** MySQL 8.4\
**Repository:** https://github.com/swatishaw1/Auth_System

------------------------------------------------------------------------

## Features

### Authentication and session management

-   Email/password registration and login.
-   JWT access and refresh tokens.
-   Refresh-token rotation and server-side revocation.
-   Logout with refresh-token revocation and cookie clearing.
-   Secure HttpOnly cookie-based token handling.
-   Configurable cookie properties: `Secure`, `HttpOnly`, `SameSite`,
    and domain.

### OAuth2

-   Google and GitHub login.
-   Custom OAuth2 user-information handling.
-   Provider-aware user creation and updates.
-   OAuth2 success and failure handlers.

### Password recovery

-   Email-based OTP generation and delivery.
-   OTP expiry and verification.
-   Password reset after successful verification.
-   BCrypt password hashing.

### Authorization and user management

-   Role-based authorization (`USER`, `ADMIN`).
-   JWT request filter and stateless Spring Security configuration.
-   User create, read, update, and delete operations.
-   Pagination and sorting.
-   DTO-based request and response models.

### Backend reliability and protection

-   Resilience4j rate limiting for sensitive authentication operations.
-   Request validation with Jakarta Bean Validation.
-   Centralized exception handling and structured API errors.
-   Service-layer business logic and Spring Data JPA repository
    abstraction.
-   Swagger/OpenAPI documentation.

### Testing and engineering workflow

-   JUnit 5 and Mockito-based backend testing.
-   Rate-limiter integration testing through Spring-managed beans and
    AOP proxies.
-   GitHub Actions workflow for backend tests, frontend checks, MySQL
    smoke tests, and Docker image builds.
-   Dockerfiles for backend and frontend.
-   Docker Compose setup for the application and MySQL.

> **Rate-limiter note:** Resilience4j rate limiters are in-memory and
> apply per limiter instance in a running application process. They are
> not distributed, per-IP, or per-user limits. Multiple backend
> instances need a shared/distributed solution for coordinated limits.

------------------------------------------------------------------------

## Tech Stack

| Category | Technology | Purpose |
|---|---|---|
| **Backend** | Java 21, Spring Boot 4.0.5 | Backend development |
| **Security** | Spring Security, JWT 0.13.0, OAuth2 | Authentication and authorization |
| **Database** | MySQL 8.4, Spring Data JPA, Hibernate | Data storage and ORM |
| **Email** | Spring Mail | OTP and recovery emails |
| **Rate Limiting** | Resilience4j | API rate limiting |
| **Testing** | JUnit 5, Mockito | Backend unit and integration testing |
| **Frontend** | React, TypeScript, Vite | User interface |
| **HTTP Client** | Axios | Frontend API requests |
| **Web Server** | Nginx | Static hosting and reverse proxy |
| **Containerization** | Docker, Docker Compose | Containerization and local orchestration |
| **CI/CD** | GitHub Actions | Automated testing and CI |
| **API Documentation** | SpringDoc OpenAPI | API documentation |
| **Build Tools** | Maven, npm | Dependency and build management |
------------------------------------------------------------------------

## Architecture

### Backend request flow

``` text
Client
  |
  v
REST Controller
  |
  v
Service Layer
  |
  v
Repository Layer
  |
  v
MySQL
```

For protected requests, Spring Security processes the request through
the JWT authentication filter before the controller is reached.

``` text
HTTP Request
  |
  v
Spring Security Filter Chain
  |
  v
JwtAuthenticationFilter
  |
  v
JWT Validation
  |
  v
Security Context
  |
  v
Controller
```

### Docker request flow

``` text
Browser
  |
  v
Frontend (Nginx)
  |
  | /api, /oauth2, /login/oauth2
  v
Spring Boot Backend
  |
  v
MySQL
```

Nginx serves the frontend and proxies API and OAuth2 routes to the
backend container.

------------------------------------------------------------------------

## Key implementation concepts

### 1. Access and refresh tokens

The application uses separate JWTs for access and refresh operations.

-   **Access token:** Authenticates requests to protected APIs and has a
    shorter lifetime.
-   **Refresh token:** Obtains a new access/refresh token pair.
-   **JTI:** A unique token identifier used to track refresh-token
    records.

The refresh token is tracked server-side, allowing the application to
revoke or replace it.

### 2. Refresh-token rotation

``` text
Client sends current refresh token
            |
            v
Validate token and server-side record
            |
            v
Generate new access and refresh tokens
            |
            v
Revoke/replace old refresh token
            |
            v
Return new token pair in cookies
```

Rotation limits the usefulness of a refresh token after it has been
used. Server-side tracking makes revocation possible.

### 3. Logout and revocation

Logout is not limited to deleting browser cookies:

1.  Read and validate the refresh token.
2.  Extract its JTI.
3.  Revoke the corresponding server-side token record.
4.  Clear authentication cookies.
5.  Clear the security context.

### 4. OAuth2 login

``` text
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
Provider-specific user information
 |
 v
Create or update application user
 |
 v
OAuth2 success handler
 |
 v
Application
```

The application distinguishes `LOCAL`, `GOOGLE`, and `GITHUB` providers.

### 5. OTP password recovery

``` text
Submit email
    |
    v
Generate and email OTP
    |
    v
Submit OTP
    |
    v
Validate OTP and expiry
    |
    v
Submit new password
    |
    v
BCrypt hash and update password
```

### 6. Resilience4j rate limiting

Rate limiting controls how many calls a named operation can accept
during a configured time period.

The application configures named limiters for sensitive operations,
including login, registration, OTP, password recovery, and refresh-token
operations.

With annotation-based integration, a Spring-managed method is
intercepted through AOP. When the limit is exceeded, Resilience4j
rejects the call with `RequestNotPermitted`; the API should translate
that into HTTP `429 Too Many Requests`.

These are application-level limits, not user-specific quotas.

### 7. Testing

-   **Unit tests:** Test individual classes or methods, using Mockito to
    isolate dependencies where appropriate.
-   **Integration tests:** Start the Spring application context and test
    interactions between Spring-managed components.
-   **Rate-limiter tests:** Verify that requests within the configured
    limit are accepted and the next request is rejected.

Rate-limiter annotations rely on Spring AOP. Tests should call the
Spring-managed bean through its proxy; directly constructing the
annotated class bypasses the proxy and does not test annotation
integration.

------------------------------------------------------------------------

## Project structure

The backend follows a layered structure. Main packages include:

``` text
authBackend/
└── src/
    ├── main/
    │   ├── java/com/example/authBackend/
    │   │   ├── Enum/
    │   │   ├── api/
    │   │   │   ├── request/
    │   │   │   └── response/
    │   │   ├── configuration/
    │   │   ├── controller/
    │   │   ├── dto/
    │   │   ├── exceptions/
    │   │   ├── filter/
    │   │   ├── helper/
    │   │   ├── model/
    │   │   ├── repository/
    │   │   ├── security/
    │   │   │   └── oauth2/
    │   │   ├── service/
    │   │   │   └── implementation/
    │   │   └── utils/
    │   └── resources/
    │       ├── application.yaml
    │       └── application-dev.yml
    ├── src/test/
    ├── Dockerfile
    └── pom.xml

authFrontend/
├── src/
├── public/
├── Dockerfile
├── nginx.conf
└── package.json

docker-compose.yml
.github/
└── workflows/
    └── ci.yml
```

The exact package and file names may evolve as the project changes.

------------------------------------------------------------------------

## API endpoints

### Authentication

  ------------------------------------------------------------------------------
  Method                  Endpoint                       Purpose
  ----------------------- ------------------------------ -----------------------
  POST                    `/api/v1/auth/register`        Register a user

  POST                    `/api/v1/auth/login`           Email/password login

  POST                    `/api/v1/auth/refresh`         Rotate tokens

  POST                    `/api/v1/auth/logout`          Revoke refresh token
                                                         and log out

  POST                    `/api/v1/auth/verifyEmail`     Start email/OTP
                                                         verification flow

  POST                    `/api/v1/auth/verifyOtp`       Verify OTP

  POST                    `/api/v1/auth/resetPassword`   Reset password
  ------------------------------------------------------------------------------

### User management

  Method   Endpoint                          Purpose
  -------- --------------------------------- ----------------------
  GET      `/api/v1/users`                   Get paginated users
  POST     `/api/v1/users`                   Create a user
  PUT      `/api/v1/users/{userId}`          Update a user
  DELETE   `/api/v1/users/{userId}`          Delete a user
  GET      `/api/v1/users/byEmail/{email}`   Find a user by email

Example pagination request:

``` http
GET /api/v1/users?pageNumber=0&pageSize=10&sortBy=name&sortOrder=asc
```

OAuth2 login is initiated through Spring Security authorization
endpoints:

``` text
/oauth2/authorization/google
/oauth2/authorization/github
```

------------------------------------------------------------------------

## Configuration

Use environment variables for secrets and environment-specific settings.
Do not commit real credentials.

The application configuration includes:

``` text
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

Create local environment files based on the variables referenced by your
configuration. Keep `.env` files out of Git. Use dummy credentials only
in test configuration.

**Never commit** JWT secrets, database passwords, SMTP credentials, or
OAuth client secrets. If a real secret is accidentally committed, rotate
it; adding it to `.gitignore` does not remove it from Git history.

------------------------------------------------------------------------

## Run locally

### Prerequisites

-   Java 21
-   Maven
-   MySQL
-   Node.js 22 and npm
-   Git

### Backend

1.  Clone the repository:

``` bash
git clone https://github.com/swatishaw1/Auth_System.git
cd Auth_System/authBackend
```

2.  Configure the required environment variables.
3.  Create the MySQL database and configure the JDBC URL and
    credentials.
4.  Start the backend:

``` bash
mvn spring-boot:run
```

Or use the Maven wrapper:

``` powershell
# Windows
.\mvnw.cmd spring-boot:run
```

``` bash
# Linux/macOS
./mvnw spring-boot:run
```

### Frontend

Open another terminal:

``` bash
cd Auth_System/authFrontend
npm ci
npm run dev
```

Configure the frontend API base URL according to the frontend
environment configuration. For the Docker/Nginx setup, the API base path
is `/api/v1`.

------------------------------------------------------------------------

## Run with Docker Compose

The repository includes Dockerfiles for the backend and frontend, plus a
Compose setup for MySQL and the application.

From the repository root, configure the root `.env` file with the
variables required by `docker-compose.yml`, then run:

``` bash
docker compose up --build -d
```

Check running containers:

``` bash
docker compose ps
```

View logs:

``` bash
docker compose logs -f
```

Stop the application:

``` bash
docker compose down
```

The frontend is exposed at:

``` text
http://localhost:3000
```

The Compose setup uses a persistent MySQL volume. **Do not use
`docker compose down -v` unless you intentionally want to delete the
database volume and its data.**

------------------------------------------------------------------------

## Tests

Run backend tests:

``` bash
cd authBackend
mvn --batch-mode clean verify
```

Run frontend checks:

``` bash
cd authFrontend
npm ci
npm run typecheck
npm run test:ci
npm run build
```

Run ESLint locally:

``` bash
npm run lint
```

The current CI workflow runs ESLint as a non-blocking step temporarily.
A green workflow therefore does not necessarily mean the lint rules
pass. Type checking, frontend tests, and the production build remain
separate checks.

------------------------------------------------------------------------

## Continuous Integration

GitHub Actions is configured in `.github/workflows/ci.yml`.

The workflow includes:

1.  **Backend tests:** Java 21 and Maven tests, with a MySQL service.
2.  **Frontend checks:** dependency installation, ESLint, TypeScript
    checking, frontend tests, and production build.
3.  **MySQL smoke tests:** verify the MySQL service, database, and basic
    SQL operations.
4.  **Docker builds:** build backend and frontend images after the
    required test jobs succeed.

This is a CI workflow. Building images in GitHub Actions does not by
itself publish them to a registry or deploy the application.

------------------------------------------------------------------------

## API documentation and Postman

Swagger UI:

``` text
http://localhost:8080/swagger-ui/index.html
```

A Postman collection is included in the backend project:

``` text
Auth System.postman_collection.json
```

Import it into Postman to exercise the authentication and
user-management endpoints.

Suggested manual authentication flow:

``` text
Register -> Login -> Call protected API -> Refresh -> Logout
```

Suggested password-recovery flow:

``` text
Start recovery -> Receive OTP -> Verify OTP -> Reset password
```

------------------------------------------------------------------------

## Security considerations and current limitations

-   Passwords are hashed with BCrypt.
-   JWT signatures, expiry, and token types are validated.
-   Refresh tokens are tracked and can be rotated or revoked.
-   Authentication cookies have configurable security attributes.
-   Secrets are supplied through environment configuration.
-   Rate limiting is in-process with Resilience4j; it is not distributed
    across multiple backend instances.
-   Cookie-based authentication requires a deliberate CSRF strategy.
    HttpOnly prevents JavaScript from reading a cookie, but does not by
    itself prevent CSRF.
-   Production deployments should use managed secrets, HTTPS, database
    migrations, monitoring, and a shared rate-limit strategy if multiple
    backend instances are used.

------------------------------------------------------------------------

## Concepts demonstrated

-   Layered architecture and REST API design.
-   Spring Security filter chain and JWT authentication.
-   Access/refresh token lifecycle, rotation, and revocation.
-   OAuth2 authorization-code login and provider-specific user mapping.
-   Cookie security and browser authentication considerations.
-   Role-based authorization.
-   OTP expiry and password recovery.
-   Rate limiting and Spring AOP.
-   Unit and integration testing with JUnit 5 and Mockito.
-   MySQL persistence, JPA, pagination, and validation.
-   Docker images, Compose networking, volumes, and Nginx reverse proxy.
-   GitHub Actions continuous integration.

------------------------------------------------------------------------

## Future improvements

-   Distributed rate limiting using a shared store such as Redis.
-   Account lockout and more granular OTP attempt controls.
-   Database migrations with Flyway or Liquibase.
-   Stronger CSRF protection and security testing for cookie-based
    flows.
-   More comprehensive end-to-end tests.
-   Centralized logging, metrics, and monitoring.
-   Automated image publishing and deployment.

------------------------------------------------------------------------

## Author

**Swati**

-   GitHub: https://github.com/swatishaw1
