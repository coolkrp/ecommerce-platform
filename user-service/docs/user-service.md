# User Service

## Overview

The User Service is responsible for managing user-related functionality in the e-commerce platform.

The service will eventually handle:

* User registration
* User login and authentication
* Authorization and role management
* User profile management
* Password reset

At the current stage, the service includes the database foundation and initial user domain model.

## Current Implementation Status

| Feature                   | Status        |
| ------------------------- | ------------- |
| Project scaffolding | ✅ Completed |
| Spring Boot service setup | ✅ Completed   |
| Java 17 configuration     | ✅ Completed   |
| MySQL connection          | ✅ Completed   |
| Flyway database migration | ✅ Completed   |
| Users table               | ✅ Completed   |
| User entity               | ✅ Completed   |
| User roles                | ✅ Completed   |
| User status               | ✅ Completed   |
| User repository           | ✅ Completed   |
| User registration         | ✅ Completed   |
| Input validation          | ✅ Completed |
| Global exception handling | ✅ Completed |
| Login                     | ✅ Completed |
| JWT generation            | ✅ Completed |
| JWT validation            | ✅ Completed |
| JWT authentication filter | ✅ Completed |
| Stateless security        | ✅ Completed |
| Role-based authorization  | ✅ Completed |
| Get current user          | ✅ Completed |
| Get user by ID            | ✅ Completed |
| Update current user       | ✅ Completed |
| Change password           | ✅ Completed |
| Password reset request    | ✅ Completed |
| Secure reset token        | ✅ Completed |
| Reset password            | ✅ Completed |
| One-time token validation | ✅ Completed |
| API documentation         | 🔄 In progress |
| Automated tests           | ⏳ Pending |

## Technology Stack

* Java 17
* Spring Boot
* Spring Data JPA
* Spring Security
* MySQL
* Flyway
* Maven
* Maven Wrapper

## Project Structure

```text
user-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ecommerce/
│   │   │       ├── UserServiceApplication.java
│   │   │       └── user/
│   │   │           ├── controller/
│   │   │           ├── dto/
│   │   │           │   ├── request/
│   │   │           │   └── response/
│   │   │           ├── entity/
│   │   │           ├── repository/
│   │   │           ├── service/
│   │   │           │   └── impl/
│   │   │           ├── exception/
│   │   │           └── config/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/
│   │           └── migration/
│   │               └── V1__create_users_table.sql
│   │
│   └── test/
│
└── pom.xml
```

## Database

The User Service uses MySQL.

Flyway is used to manage database schema changes. Hibernate is configured to validate the database schema rather than create or modify it automatically.

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate

  flyway:
    enabled: true
```

### Users Table

The initial migration creates the following table:

```text
users
├── id
├── first_name
├── last_name
├── email
├── password_hash
├── role
├── status
├── created_at
└── updated_at
```

The user's email has a unique constraint to prevent duplicate accounts.

## User Domain Model

The `User` entity maps to the `users` table.

### User Fields

| Field          | Description                                 |
| -------------- | ------------------------------------------- |
| `id`           | Unique user identifier                      |
| `firstName`    | User's first name                           |
| `lastName`     | User's last name                            |
| `email`        | User's unique email address                 |
| `passwordHash` | BCrypt/password hash stored in the database |
| `role`         | User authorization role                     |
| `status`       | Current user account status                 |
| `createdAt`    | Account creation timestamp                  |
| `updatedAt`    | Last update timestamp                       |

The password itself must never be stored in plaintext or returned through an API response.

## Roles

The current user roles are:

```text
CUSTOMER
ADMIN
```

The role is stored using its string representation rather than its numeric enum position.

## User Status

The current account statuses are:

```text
ACTIVE
INACTIVE
LOCKED
```

### Password Reset Tokens

The `password_reset_tokens` table stores hashed, short-lived password reset tokens.

| Column | Description |
|---|---|
| `id` | Primary key |
| `user_id` | User associated with the reset request |
| `token_hash` | SHA-256 hash of the reset token |
| `expires_at` | Token expiration time |
| `used` | Indicates whether the token has already been consumed |
| `created_at` | Token creation timestamp |

## UserRepository

`UserRepository` extends Spring Data JPA's `JpaRepository`.

Currently supported operations include:

```java
boolean existsByEmail(String email);

Optional<User> findByEmail(String email);
```

### Usage

`existsByEmail()` will be used during registration to detect duplicate accounts.

`findByEmail()` will later be used during login and authentication.

## PasswordResetTokenRepository

`PasswordResetTokenRepository` provides persistence operations for password reset tokens.

Key operations include:

- Find a reset token by its SHA-256 hash.
- Remove previous reset tokens for a user before creating a new one.

## Registration Flow

The planned registration flow is:

```text
Client
   |
   | POST /api/v1/users/register
   v
User Controller
   |
   v
Request Validation
   |
   v
User Service
   |
   v
Check Email
   |
   +---- Existing ----> 409 Conflict
   |
   +---- New
          |
          v
     Hash Password
          |
          v
      Save User
          |
          v
      User Response
```

Registration is the next feature to be implemented.

## Authentication Flow

The User Service uses JWT-based stateless authentication.

```text
Login
  │
  ▼
Validate email/password
  │
  ▼
Generate JWT
  │
  ▼
Client stores token
  │
  ▼
Authorization: Bearer <JWT>
  │
  ▼
JwtAuthenticationFilter
  │
  ├── Validate signature
  ├── Validate expiration
  └── Extract user ID and role
  │
  ▼
SecurityContext
  │
  ▼
Protected API
```

## Reset Password Flow
```text
POST /auth/forgot-password
            │
            ▼
       Find User
            │
            ▼
 Generate Secure Random Token
            │
       ┌────┴────┐
       ▼         ▼
   Raw Token   SHA-256
       │         │
       │         ▼
       │      MySQL
       │    token_hash
       │
       ▼
 Notification Service
       │
       ▼
   Reset Link
       │
       ▼
POST /auth/reset-password
       │
       ▼
 SHA-256(raw token)
       │
       ▼
 Find token_hash
       │
   ┌───┴──────────────┐
   │                  │
 Valid              Invalid
   │                  │
   ▼                  ▼
Check expiry/used    400
   │
   ▼
Hash new password
   │
   ▼
Update User
   │
   ▼
Mark token as used
```

## Security

### Roles

Role-based authorization is implemented using Spring Security method-level authorization.

**Example:**

```java
@PreAuthorize("hasRole('ADMIN')")
```

**HTTP Security Responses**
Situation	Status
* No authentication	401 Unauthorized 
* Invalid/expired JWT	401 Unauthorized
* Authenticated but insufficient permissions	403 Forbidden
* Valid authenticated request	2xx

### Password Change and JWT

Changing a password updates the stored password hash.

Existing JWT access tokens remain valid until their normal expiration because the current authentication implementation is stateless and does not maintain server-side token sessions or revocation state.

A future token-revocation mechanism can be introduced if immediate invalidation of existing tokens after a password change is required.

### Security Considerations

- Reset tokens are generated using `SecureRandom`.
- Tokens contain 256 bits of randomness.
- Only the SHA-256 hash is stored in MySQL.
- Tokens expire after a limited period.
- A reset token can only be used once.
- Forgot-password does not reveal whether an email exists.
- Reset-password does not require an authenticated JWT.
- The new password is stored using BCrypt.
- Existing JWTs are not automatically revoked by password reset in the current implementation.

## API Endpoints

### Register User

**Endpoint**

`POST /api/v1/users/register`

**Description**

Creates a new customer account.

**Request**

```json
Authentication: Public
{
  "firstName": "Komal",
  "lastName": "Pawar",
  "email": "komal@example.com",
  "password": "Password@123"
}
```

**Success Response**
```json
Success: 201 Created
{
  "id": 1,
  "firstName": "Komal",
  "lastName": "Pawar",
  "email": "komal@example.com",
  "role": "CUSTOMER",
  "status": "ACTIVE",
  "createdAt": "2026-08-08T13:28:38.198620500Z",
  "updatedAt": "2026-08-08T13:28:38.198620500Z"
}
```

### Get Current User

**Endpoint**

`GET /api/v1/users/me`

**Description**

Returns the profile of the authenticated user.

**Response**
```json
Authentication: Bearer JWT required
Success: 200 OK
{
  "id": 1,
  "firstName": "Komal",
  "lastName": "Pawar",
  "email": "komal@example.com",
  "role": "CUSTOMER"
}
```

### Get User by Id

**Endpoint**

`GET /api/v1/users/{id}`

**Description**

Returns a user's profile.

**Authorization:**

CUSTOMER can access their own profile.
ADMIN can access any user's profile.

```
Authentication: Bearer JWT required
```
Responses:

Status Code and description
* 200	User found
* 401	Authentication required
* 403	User does not have permission
* 404	User not found

### Update Current User

**Endpoint**

`PUT /api/v1/users/me`

**Description**

Updates the authenticated user's profile.

**Request**
```json
Authentication: Bearer JWT required
{
  "firstName": "Komal",
  "lastName": "Pawar"
}
```

The following fields cannot be modified through this endpoint:

* `id`
* `email`
* `password`
* `role`
* `status`

Success: 200 OK

Validation: Invalid request data returns 400 Bad Request.

### Change Password

**Endpoint**

`PATCH /api/v1/users/me/password`

Changes the password of the currently authenticated user.

**Authentication:** Bearer JWT required

**Request**

```json
{
  "currentPassword": "Password@123",
  "newPassword": "NewPassword@456"
}
```

**Password handling:**

The current password is verified using BCrypt.
The new password is stored as a BCrypt hash.
Plain-text passwords are never stored or returned.

| Status | Description                   |
| ------ | ----------------------------- |
| 204    | Password changed successfully |
| 400    | Invalid current password      |
| 400    | Invalid request/new password  |
| 401    | Authentication required       |
| 404    | User not found                |

### Login

**Endpoint**

`POST /api/v1/auth/login`

**Description**

Authenticates an existing user and returns a JWT access token.

**Request**

```json
Authentication: Public
{
  "email": "user@example.com",
  "password": "Password@123"
}
```

**Response**

```json
Success: 200 OK
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

### Forgot Password

**Endpoint**

`POST /api/v1/auth/forgot-password`

**Description**

Initiates a secure password-reset flow for the supplied email address.

**Authentication:** Public

**Request**

```json
{
  "email": "user@example.com"
}
```

**Response**
```json
Success: 200 OK
{
  "message": "If the account exists, a password reset link has been sent."
}
```

### Reset Password

## Endpoint

`POST /api/v1/auth/reset-password`

**Description**

Resets the password using a secure, short-lived, one-time reset token.

**Authentication:** Public

**Request**
```json
{
  "token": "<reset-token>",
  "newPassword": "NewPassword@456"
}
```

The reset token is:

* Cryptographically generated using SecureRandom.
* Stored as a SHA-256 hash.
* Time-limited.
* Single-use.
* Invalidated after successful password reset.

Success: 204 No Content

Invalid, expired, or already-used tokens return 400 Bad Request.


## Local Development

### Start MySQL

From the project root:

```bash
docker compose up -d mysql
```

Verify:

```bash
docker compose ps
```

### Build the Project

From the project root:

```bash
./mvnw clean install
```

### Run User Service

```bash
./mvnw -pl user-service spring-boot:run
```

The User Service runs on its configured application port.

## Database Migration

Flyway migrations are located under:

```text
src/main/resources/db/migration/
```

The first migration is:

```text
V1__create_users_table.sql
```

After starting the service, Flyway creates its migration history table and applies pending migrations.

To verify the database:

```sql
SHOW TABLES;
```

The database should contain:

```text
flyway_schema_history
users
```

## Development Approach

The User Service is being implemented incrementally.

Each feature follows:

```text
Implementation
      ↓
Local testing
      ↓
Documentation update
      ↓
Git checkpoint
      ↓
Next feature
```

This keeps the implementation and documentation synchronized throughout development.

## Planned Next Steps

1. Create registration request DTO.
2. Create user response DTO.
3. Configure password hashing.
4. Implement registration service.
5. Implement registration controller.
6. Add validation and exception handling.
7. Test registration through Swagger/API client.
8. Add automated tests.
9. Implement login and JWT authentication.
10. Implement authorization and RBAC.
11. Implement profile management.
12. Implement password reset.
