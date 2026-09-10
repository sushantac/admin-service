# Admin Service

Admin dashboard and management for the e-commerce platform: business metrics, order management, user lookup, and audit trail. Data is driven by Kafka events.

## API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/admin/dashboard` | KPIs (orders, revenue, users, products) |
| GET | `/api/v1/admin/orders` | Order list w/ status filter |
| GET | `/api/v1/admin/orders/{id}` | Order detail |
| PUT | `/api/v1/admin/orders/{id}/status` | Update status |
| GET | `/api/v1/admin/users` | User list w/ search |
| GET | `/api/v1/admin/users/{id}` | User + order count |

## Events

Consumed: `order.placed`, `order.status.changed`, `auth.user.registered` (idempotent consumers).

## Stack

Java 21, Spring Boot 3.4, Liquibase (schema `admin`), PostgreSQL, Kafka.

## Development

```bash
./mvnw spring-boot:run
# http://localhost:8085
```

Full local orchestration lives in the `sdlc` repo (`make dev`).