# E-Commerce Platform

Backend implementation aligned with the supplied Ecommerce PRD/HLD.

## Business Services
- User Management: registration, login, profile management, password reset; MySQL + Kafka
- Product Catalog: products, details, categories, keyword search; MySQL + Elasticsearch
- Cart: cart management; MongoDB + Redis
- Order Management: checkout, orders, history, tracking; MySQL + Kafka
- Payment: multiple payment methods, transactions, receipts; MySQL + Kafka
- Notification: email/SMS-ready notifications; Kafka + Amazon SES integration point

## Infrastructure
Kong API Gateway, AWS ELB for deployment, MySQL, MongoDB, Redis, Kafka, Elasticsearch.

## Local Development
Prerequisites: JDK 17, Maven 3.9+, Docker Desktop.

```bash
docker compose up -d
mvn clean verify
```

Swagger example: `http://localhost:8082/swagger-ui.html`
