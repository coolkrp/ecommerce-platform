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
- Elasticsearch-based product search
- Elasticsearch product indexing
- Elasticsearch product reindexing
- Kafka-based product event publishing
- Kafka-based asynchronous Elasticsearch synchronization

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
| Elasticsearch integration	|✅ Completed |
| Elasticsearch product indexing |	✅ Completed |
| Elasticsearch product search |	✅ Completed |
| Elasticsearch reindexing |	✅ Completed |
| Kafka product events |	✅ Completed |
| Kafka JSON serialization/deserialization |	✅ Completed |
| Kafka Elasticsearch synchronization |	✅ Completed |
| Pagination |	⏳ Pending |
| Product filtering	| ⏳ Pending |
| Typo-tolerant search |	⏳ Pending |
| Automated tests |	⏳ Pending |
| API documentation |	🔄 In progress |

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
|   |   |           ├── event/
│   │   │           ├── exception/
|   |   |           └──kafka/
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

It supports:

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

The service uses:

```yaml
spring:
  jpa:
    open-in-view: false
```

For product reads, `@EntityGraph` explicitly fetches the Category when required.

For product updates, the service method is transactional because the operation reads and modifies Product and Category data within one business operation.

This avoids changing the relationship to `EAGER` simply to solve response serialization.

## Product CRUD Flow

### Create Product

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
ProductEvent 
   | 
   v 
 Kafka 
   | 
   v 
ProductEventConsumer 
   | 
   v 
Elasticsearch
   |
   v
ProductResponse
```
The API response is returned from the Product Service after the product is persisted.

Elasticsearch synchronization happens asynchronously through Kafka.

### Update Product

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
### Delete Product

```text
DELETE /api/v1/products/{id} 
            | 
            v 
      Find Product 
            | 
            v 
      Delete Product 
            | 
            v 
          MySQL 
            | 
            v 
      PRODUCT_DELETED 
            | 
            v 
          Kafka 
            | 
            v
    roductEventConsumer 
            | 
            v 
Delete Elasticsearch document
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

### Product APIs

| Method |	Endpoint	|Description |
|---|---|---|
| POST    |	/api/v1/products  |	Create product  |
| GET	| /api/v1/products  |	Get all products  |
| GET |	/api/v1/products/{id} |	Get product by ID |
| PUT |	/api/v1/products/{id} |	Update product  |
| DELETE  |	/api/v1/products/{id} |	Delete product |
| GET |	/api/v1/products/search?q={query} |	Search products |
| POST  |	/api/v1/products/reindex  |	Reindex products in Elasticsearch |

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

### Product Search

```http
GET /api/v1/products/search?q={keyword}
```
Success:
```text
200 OK
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


## Elasticsearch Integration

Elasticsearch is used as the search engine for the Product Catalog.

MySQL remains the primary source of truth for Product data.

The Product Service maintains an Elasticsearch representation of the Product through ProductDocument.

### Architecture

```text
            ┌──────────────┐
            │    MySQL     │
            │ Source of    │
            │    Truth     │
            └──────┬───────┘
                   │
              Product CRUD
                   |
                   ▼
            ┌──────────────┐
            │   Product    │
            │   Service    │
            └──────┬───────┘
                   │
            Product Event
                   |
                   ▼
            ┌──────────────┐
            │     Kafka    |
            |product-events│
            └──────┬───────┘
                   │
                   ▼
            ┌──────────────┐
            │ ProductEvent |
            |  consumer    │
            └──────┬───────┘
                   │
                   ▼
         ┌──────────────────┐
         │  Elasticsearch   │
         └──────────────────┘
```
## ProductDocument

ProductDocument represents the Elasticsearch representation of a product.

The document contains product fields required for search and product retrieval, including:

- Product ID
- Name
- Description
- SKU
- Price
- Stock quantity
- Category information
- Image URL
- Active status

The Elasticsearch document is separate from the JPA Product entity.

This keeps database persistence concerns separate from search-index concerns.

## ProductSearchRepository

ProductSearchRepository is responsible for Elasticsearch document persistence and retrieval.

It is separate from ProductRepository.

```text
ProductRepository
        |
        v
      MySQL

ProductSearchRepository
        |
        v
 Elasticsearch
```

The separation prevents Elasticsearch queries from being interpreted as Spring Data JPA property-derived queries.

## ProductSearchService

ProductSearchService provides Elasticsearch-related operations including:

- Index product
- Search products
- Delete product document
- Reindex products

The service can index a product from a ProductEvent without reading the Product entity again from MySQL.

This is important for the Kafka-based event-driven architecture because the Kafka event contains the product snapshot required to construct the search document.

## Product Search

Products can be searched using:

GET /api/v1/products/search?q=iphone

The search request is handled by the Product search layer and queries Elasticsearch.

Example:

```text
Client
   |
   | GET /api/v1/products/search?q=iphone
   v
ProductController
   |
   v
ProductSearchService
   |
   v
ProductSearchRepository
   |
   v
Elasticsearch
```

## Elasticsearch Reindexing

A reindex operation is provided to rebuild the Elasticsearch product index from MySQL data.

The reindex flow is:
```text
MySQL Products
      |
      v
ProductService
      |
      v
ProductSearchService
      |
      v
Elasticsearch
```

Reindexing is useful when the Elasticsearch index is lost, corrupted, or needs to be rebuilt.

## Kafka Integration

Kafka is used for asynchronous Product events.

The Product Service publishes events whenever Product data changes.

### Kafka Topic
product-events

The topic is used for:

- Product creation events
- Product update events
- Product deletion events

## ProductEvent

ProductEvent represents a Product domain event published to Kafka.

The event contains the product snapshot required by the downstream consumer.

Example structure:
```json
{
  "eventType": "PRODUCT_CREATED",
  "productId": 4,
  "name": "Search Test IPad",
  "description": "Product created to test kafka event",
  "sku": "SEARCH-TEST-001",
  "price": 10999.99,
  "stockQuantity": 10,
  "categoryId": 2,
  "imageUrl": "https://example.com/test-ipad.jpg",
  "active": true
}
```

## ProductEventType

Supported event types:

**PRODUCT_CREATED**
**PRODUCT_UPDATED**
**PRODUCT_DELETED**

The event type determines how the consumer updates Elasticsearch.

## ProductEventProducer

ProductEventProducer is responsible for publishing Product events to Kafka.

The producer publishes to:

product-events

The Product ID is used as the Kafka message key.

Conceptually:
```text
ProductService
      |
      v
ProductEventProducer
      |
      v
KafkaTemplate
      |
      v
product-events
```

## ProductEventConsumer

ProductEventConsumer listens to:

product-events

using the consumer group:

product-search-consumer

The consumer processes events according to their type.
```text
PRODUCT_CREATED
        |
        v
Index Product in Elasticsearch

PRODUCT_UPDATED
        |
        v
Update Product in Elasticsearch

PRODUCT_DELETED
        |
        v
Delete Product from Elasticsearch
```

## Kafka Event Processing

**PRODUCT_CREATED**
```text
Product created
      |
      v
MySQL
      |
      v
PRODUCT_CREATED
      |
      v
Kafka
      |
      v
ProductEventConsumer
      |
      v
Elasticsearch index
```

**PRODUCT_UPDATED**
```text
Product updated
      |
      v
MySQL
      |
      v
PRODUCT_UPDATED
      |
      v
Kafka
      |
      v
ProductEventConsumer
      |
      v
Elasticsearch update
```
**PRODUCT_DELETED**
```text
Product deleted
      |
      v
MySQL
      |
      v
PRODUCT_DELETED
      |
      v
Kafka
      |
      v
ProductEventConsumer
      |
      v
Elasticsearch document deletion
```

## Kafka Serialization and Deserialization

The producer uses Spring Kafka's JSON serializer.

```yaml
spring:
  kafka:
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

The consumer uses the corresponding JSON deserializer.

```yaml
spring:
  kafka:
    consumer:
      group-id: product-search-consumer
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: com.ecommerce.product.event
        spring.json.value.default.type: com.ecommerce.product.event.ProductEvent
```

The consumer configuration is required so the JSON Kafka payload can be converted into a ProductEvent object.

## Eventual Consistency

The Product Service uses an eventually consistent search architecture.

MySQL is the primary source of truth.

Elasticsearch is an asynchronously updated search/read model.

For a Product update:
```text
Client
  |
  v
Product Service
  |
  v
MySQL
  |
  v
Kafka
  |
  v
ProductEventConsumer
  |
  v
Elasticsearch
```

Therefore, there can be a small delay between the successful Product API response and the corresponding Elasticsearch search result.

This is expected behavior for asynchronous event processing.

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
    producer: 
      key-serializer: org.apache.kafka.common.serialization.StringSerializer 
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer 
    
    consumer: 
      group-id: product-search-consumer 
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer 
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer 
      properties: 
        spring.json.trusted.packages: com.ecommerce.product.event 
        spring.json.value.default.type: com.ecommerce.product.event.ProductEvent 
    flyway: enabled: true
    
server: port: 8083
```
## Infrastructure Dependencies

Product Service requires the following infrastructure:

MySQL
Elasticsearch
Kafka

The expected local endpoints are:

MySQL
localhost:3306

Elasticsearch
http://localhost:9200

Kafka
localhost:9092

The services are defined in the project's Docker Compose configuration.

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

Before testing Elasticsearch or Kafka functionality, ensure the corresponding infrastructure containers are running.

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

**Category**
- Category POST
- Category GET
- Category CRUD
- Invalid category handling
- Category not-found handling

**Product CRUD**
- Product POST
- Product GET all
- Product GET by ID
- Product PUT
- Product DELETE
- Validation failures
- Duplicate SKU handling
- Product not-found handling
- Global exception handling

**Elasticsearch**
- Product indexing
- Product search
- Product reindexing
- Elasticsearch document update
- Elasticsearch document deletion

**Kafka**
- Kafka broker connectivity
- product-events topic creation
- PRODUCT_CREATED event publishing
- PRODUCT_UPDATED event publishing
- PRODUCT_DELETED event publishing
- Kafka JSON serialization
- Kafka JSON deserialization
- ProductEventConsumer
- Kafka-based Elasticsearch indexing
- Kafka-based Elasticsearch updating
- Kafka-based Elasticsearch deletion

The final event-driven flow was also tested with the direct Elasticsearch calls removed from Product CRUD operations.

## Kafka Integration Verification

The following event sequence was verified:
```
PRODUCT_CREATED
PRODUCT_UPDATED
PRODUCT_DELETED
```
Example:
```json
{
  "eventType": "PRODUCT_CREATED",
  "productId": 4,
  "name": "Search Test IPad",
  "description": "Product created to test kafka event",
  "sku": "SEARCH-TEST-001",
  "price": 10999.99,
  "stockQuantity": 10,
  "categoryId": 2,
  "imageUrl": "https://example.com/test-ipad.jpg",
  "active": true
}
```

The corresponding Kafka consumer successfully processed all three event types.

## Final Product Service Architecture

```text
                         ┌──────────────────┐
                         │      Client      │
                         └────────┬─────────┘
                                  │
                                  ▼
                       ┌────────────────────┐
                       │ Product Controller │
                       └─────────┬──────────┘
                                 │
                                 ▼
                       ┌────────────────────┐
                       │  Product Service   │
                       └─────────┬──────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
                    ▼                         ▼
             ┌──────────────┐         ┌──────────────┐
             │    MySQL     │         │Product Event │
             │ Source of    │         │   Producer   │
             │    Truth     │         └──────┬───────┘
             └──────────────┘                │
                                             ▼
                                      ┌──────────────┐
                                      │    Kafka     │
                                      │product-events│
                                      └──────┬───────┘
                                             │
                                             ▼
                                      ┌──────────────┐
                                      │Product Event │
                                      │   Consumer   │
                                      └──────┬───────┘
                                             │
                                             ▼
                                      ┌──────────────┐
                                      │Elasticsearch │
                                      │ Search Model │
                                      └──────────────┘
```

## Data Ownership

The Product Service follows these data ownership rules:
```text
MySQL
  ↓
Primary Product data

Kafka
  ↓
Product change events

Elasticsearch
  ↓
Search/read model
```

MySQL remains authoritative for Product state.

Elasticsearch should not be treated as the primary transactional data store.

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
Integration verification 
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
3. Add typo-tolerant product search.
4. Add automated unit and integration tests.
5. Complete API/OpenAPI documentation.
6. Validate Product Service through the API Gateway.
7. Review Kafka reliability and failure-handling behavior.
8. Consider retry and dead-letter handling for failed Kafka events.

## PRD/HLD Alignment

The Product Service implementation aligns with the Product Catalog requirements:

- Browse products by category — Category and Product CRUD foundation completed.
- Product details — Product entity and APIs completed.
- Keyword search — Elasticsearch-based search implemented.
- Fast search — Elasticsearch integration implemented.
- Asynchronous product synchronization — Kafka integration implemented.
- MySQL Product Catalog persistence — implemented.
- Elasticsearch search/read model — implemented.
- Kafka event-driven communication — implemented

The remaining search enhancement is typo-tolerant search, which has not yet been implemented or validated.
