# Product Service

## Overview

The Product Service is responsible for managing the product catalog of the e-commerce platform.

The service currently provides:

- Category management
- Product management
- Product categorization
- Product validation
- Product CRUD APIs
- Database persistence using MySQL
- Database schema management using Flyway
- Centralized exception handling

The Product Catalog requirements in the PRD include browsing products by category, product details, and keyword-based search. Elasticsearch is planned for fast full-text product search and typo correction.

## Current Implementation Status

| Feature | Status |
|---|---|
| Project scaffolding | ✅ Completed |
| Spring Boot service setup | ✅ Completed |
| Java 17 configuration | ✅ Completed |
| MySQL connection | ✅ Completed |
| Flyway database migration | ✅ Completed |
| Category entity | ✅ Completed |
| Category repository | ✅ Completed |
| Category request/response DTOs | ✅ Completed |
| Category CRUD | ✅ Completed |
| Product entity | ✅ Completed |
| Product repository | ✅ Completed |
| Product request/response DTOs | ✅ Completed |
| Product CRUD | ✅ Completed |
| Input validation | ✅ Completed |
| Category validation | ✅ Completed |
| SKU uniqueness validation | ✅ Completed |
| Global exception handling | ✅ Completed |
| Product-specific exceptions | ✅ Completed |
| Lazy-loading handling | ✅ Completed |
| Pagination | ⏳ Pending |
| Product filtering | ⏳ Pending |
| Elasticsearch integration | ⏳ Pending |
| Keyword/full-text search | ⏳ Pending |
| Typo-tolerant search | ⏳ Pending |
| Kafka product events | ⏳ Pending |
| Automated tests | ⏳ Pending |
| API documentation | 🔄 In progress |

## Technology Stack

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- MySQL
- Flyway
- Elasticsearch
- Spring Data Elasticsearch
- Apache Kafka
- Spring Validation
- Springdoc OpenAPI
- Maven
- Maven Wrapper

## Project Structure

```text
product-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ecommerce/
│   │   │       ├── ProductServiceApplication.java
│   │   │       └── product/
│   │   │           ├── controller/
│   │   │           ├── dto/
│   │   │           │   ├── request/
│   │   │           │   └── response/
│   │   │           ├── entity/
│   │   │           ├── repository/
│   │   │           ├── service/
│   │   │           └── exception/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/
│   │           └── migration/
│   │
│   └── test/
│
├── target/
└── pom.xml
```

## Database

The Product Service uses MySQL for structured product catalog data.

Hibernate is configured to validate the schema rather than create or modify it automatically:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
```

The Product Service uses its own product database/schema rather than reusing the User Service schema.

### Main Domain Tables

```text
categories
├── id
├── name
├── description
├── created_at
└── updated_at

products
├── id
├── name
├── description
├── sku
├── price
├── stock_quantity
├── category_id
├── image_url
├── active
├── created_at
└── updated_at
```

`products.category_id` references `categories.id`.

## Category Domain

Categories are used to organize products and support category-based product browsing.

The Category domain includes:

- Entity
- Repository
- Request DTO
- Response DTO
- Service
- Controller
- Validation
- Not-found and duplicate handling

## Product Domain

The Product entity contains the core catalog information:

| Field | Description |
|---|---|
| `id` | Unique product identifier |
| `name` | Product name |
| `description` | Product description |
| `sku` | Unique stock keeping unit |
| `price` | Product price |
| `stockQuantity` | Available stock |
| `category` | Associated product category |
| `imageUrl` | Product image URL |
| `active` | Product availability status |
| `createdAt` | Creation timestamp |
| `updatedAt` | Last update timestamp |

Money is represented using `BigDecimal` to avoid floating-point precision issues.

## DTO Structure

Request and response DTOs are maintained separately.

```text
dto/
├── request/
│   ├── CategoryRequest.java
│   └── ProductRequest.java
└── response/
    ├── CategoryResponse.java
    └── ProductResponse.java
```

`ProductRequest` accepts `categoryId` rather than exposing the Category entity directly.

Example:

```json
{
  "name": "iPhone 17",
  "description": "Apple smartphone",
  "sku": "IPHONE-17-001",
  "price": 79999.00,
  "stockQuantity": 10,
  "categoryId": 1,
  "imageUrl": "https://example.com/iphone17.jpg",
  "active": true
}
```

## Repositories

`ProductRepository` extends Spring Data JPA's `JpaRepository`.

It currently supports:

```java
boolean existsBySku(String sku);

List<Product> findAll();

Optional<Product> findById(Long id);
```

The Product repository uses `@EntityGraph(attributePaths = "category")` for product reads so the category can be included in the response while retaining a lazy entity relationship.

## JPA Fetch Strategy

The Product-to-Category relationship is kept lazy.

```text
Product
   |
   | ManyToOne
   v
Category
```

The service keeps:

```yaml
spring:
  jpa:
    open-in-view: false
```

For product reads, `@EntityGraph` explicitly fetches the Category when required.

For product updates, the service method is transactional because the operation reads and modifies Product and Category data within one business operation.

This avoids changing the relationship to `EAGER` simply to solve response serialization.

## Product CRUD Flow

```text
Client
   |
   | POST /api/v1/products
   v
ProductController
   |
   v
Request Validation
   |
   v
ProductService
   |
   +--> Check SKU uniqueness
   |
   +--> Find Category
   |
   +--> Create Product
   |
   v
ProductRepository
   |
   v
MySQL
   |
   v
ProductResponse
```

### Update Flow

```text
PUT /api/v1/products/{id}
          |
          v
Find Product
          |
          v
Validate SKU
          |
          v
Find Category
          |
          v
Update Product
          |
          v
Save Product
          |
          v
ProductResponse
```

## Validation and Exception Handling

Product requests use Jakarta Bean Validation.

Examples:

- Product name is required.
- SKU is required.
- Price must be greater than zero.
- Stock quantity cannot be negative.
- Category ID is required.
- Category must exist.
- SKU must be unique.

Product-specific exceptions:

```text
ProductNotFoundException
ProductAlreadyExistsException
```

These are handled by the existing `GlobalExceptionHandler`.

Expected responses include:

| Situation | Status |
|---|---:|
| Invalid request | 400 |
| Product not found | 404 |
| Category not found | 404 |
| Duplicate SKU | 409 |
| Successful create | 201 |
| Successful read/update | 200 |
| Successful delete | 204 |

## API Endpoints

### Create Product

```http
POST /api/v1/products
```

Example request:

```json
{
  "name": "iPhone 17",
  "description": "Apple smartphone",
  "sku": "IPHONE-17-001",
  "price": 79999.00,
  "stockQuantity": 10,
  "categoryId": 1,
  "imageUrl": "https://example.com/iphone17.jpg",
  "active": true
}
```

Success:

```text
201 Created
```

### Get All Products

```http
GET /api/v1/products
```

Success:

```text
200 OK
```

### Get Product by ID

```http
GET /api/v1/products/{id}
```

Success:

```text
200 OK
```

### Update Product

```http
PUT /api/v1/products/{id}
```

Success:

```text
200 OK
```

### Delete Product

```http
DELETE /api/v1/products/{id}
```

Success:

```text
204 No Content
```

## Category API Endpoints

The Product Service also exposes Category CRUD APIs:

```text
POST   /api/v1/categories
GET    /api/v1/categories
GET    /api/v1/categories/{id}
PUT    /api/v1/categories/{id}
DELETE /api/v1/categories/{id}
```

All Category CRUD operations have been implemented and tested.

## Application Configuration

Current Product Service configuration:

```yaml
spring:
  application:
    name: product-service

  datasource:
    url: jdbc:mysql://localhost:3306/ecommerce
    username: root
    password: root

  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false

  elasticsearch:
    uris: http://localhost:9200

  kafka:
    bootstrap-servers: localhost:9092

  flyway:
    enabled: true

server:
  port: 8083
```

Elasticsearch and Kafka are configured as infrastructure dependencies but their Product Service functionality is not yet implemented.

## Local Development

From the project root:

```bash
./mvnw clean install
```

Run Product Service:

```bash
./mvnw -pl product-service spring-boot:run
```

Product Service runs on:

```text
http://localhost:8083
```

## Database Migration

Flyway migrations are located under:

```text
product-service/src/main/resources/db/migration/
```

Flyway maintains migration history in:

```text
flyway_schema_history
```

The Product Service database must contain the category and product tables required by the JPA entities.

Hibernate uses:

```text
ddl-auto: validate
```

so schema changes should be made through Flyway migrations.

## Testing Completed

The following Product Service scenarios have been manually verified:

- Category POST
- Category GET
- Category CRUD
- Product POST
- Product GET all
- Product GET by ID
- Product PUT
- Product DELETE
- Validation failures
- Invalid category handling
- Duplicate SKU handling
- Product not-found handling
- Category not-found handling
- Global exception handling

## Development Approach

The Product Service is being implemented incrementally.

Each feature follows:

```text
Implementation
      ↓
Compilation
      ↓
Local testing
      ↓
Documentation update
      ↓
Git checkpoint
      ↓
Next feature
```

This keeps the implementation and documentation synchronized.

## Planned Next Steps

1. Add pagination for product listing.
2. Add product filtering by category and other catalog attributes.
3. Implement Elasticsearch product indexing.
4. Implement keyword-based product search.
5. Add full-text and typo-tolerant search.
6. Integrate Kafka product events where required by the architecture.
7. Add automated tests.
8. Complete API/OpenAPI documentation.
9. Validate Product Service through the API Gateway.

## PRD Alignment

The Product Service implementation aligns with the Product Catalog requirements:

- Browse products by category — Category and Product CRUD foundation completed.
- Product details — Product entity and APIs completed.
- Keyword search — pending.
- Elasticsearch-based fast/full-text search and typo correction — pending.

The PRD/HLD specifies MySQL for the Product Catalog and Elasticsearch for fast product search, including full-text search and typo correction.
