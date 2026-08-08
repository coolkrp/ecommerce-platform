# Day 1 – Foundation aligned to supplied PRD/HLD

The source HLD defines six business microservices: User Management, Product Catalog, Cart, Order Management, Payment and Notification. Kong is the API Gateway; AWS ELB is the production load-balancing layer. MySQL is used for structured user/product/order/payment data, MongoDB for cart data, Redis for cart caching, Kafka for asynchronous communication, and Elasticsearch for product search.

Authentication and session management are part of User Management in the supplied HLD, so authentication/authorization will be implemented in `user-service` rather than creating a separate IAM service.

Initial event concepts: user registration, cart update, order creation, payment confirmation and order updates. Exact Kafka contracts will be finalized during implementation.
