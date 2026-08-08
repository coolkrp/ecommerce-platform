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
| Spring Boot service setup | ✅ Completed   |
| Java 17 configuration     | ✅ Completed   |
| MySQL connection          | ✅ Completed   |
| Flyway database migration | ✅ Completed   |
| Users table               | ✅ Completed   |
| User entity               | ✅ Completed   |
| User roles                | ✅ Completed   |
| User status               | ✅ Completed   |
| User repository           | ✅ Completed   |
| User registration API     | ⏳ In progress |
| Login                     | ⏳ Planned     |
| JWT authentication        | ⏳ Planned     |
| Refresh token             | ⏳ Planned     |
| RBAC                      | ⏳ Planned     |
| Profile management        | ⏳ Planned     |
| Password reset            | ⏳ Planned     |

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

## Repository

`UserRepository` extends Spring Data JPA's `JpaRepository`.

Currently supported operations include:

```java
boolean existsByEmail(String email);

Optional<User> findByEmail(String email);
```

### Usage

`existsByEmail()` will be used during registration to detect duplicate accounts.

`findByEmail()` will later be used during login and authentication.

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
