# Kafka Topics – Initial Design

| Topic | Producer | Consumers | Purpose |
|---|---|---|---|
| user.registered | User Management | Notification | Registration confirmation / welcome communication |
| cart.updated | Cart | Future consumers | Cart activity event |
| order.created | Order Management | Payment | Trigger payment processing |
| payment.confirmed | Payment | Order Management, Notification | Confirm payment and notify user |
| order.updated | Order Management | Notification | Order status / tracking updates |
