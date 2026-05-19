# POS System — Backend API

A production-ready Point of Sale backend built with **Spring Boot 3.x**, **PostgreSQL 15**, and **JWT stateless auth**. Multi-module Maven project covering products, inventory, orders, payments, customers, suppliers, and reporting.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security 6 + JWT (jjwt 0.12.6) |
| DB Access | Spring Data JPA + Hibernate 6 |
| Database | PostgreSQL 15 |
| Migrations | Flyway |
| Connection Pool | HikariCP |
| API Docs | Springdoc OpenAPI / Swagger UI |
| Build | Maven (multi-module) |
| Container | Docker + docker-compose |
| Monitoring | Spring Actuator |

---

## Project Structure

```
pos-backend/
├── pos-common/       # Shared DTOs, exceptions, base entity
├── pos-auth/         # JWT, Spring Security, login API, user management
├── pos-product/      # Product, Category, barcode lookup
├── pos-inventory/    # Stock levels, adjustments, low-stock alerts
├── pos-order/        # Order flow, cart, discount, receipt
├── pos-payment/      # Payment recording (cash / card / QR)
├── pos-customer/     # Customer profile, loyalty points
├── pos-supplier/     # Supplier, Purchase Order, goods receiving
├── pos-report/       # Sales reports, daily close, CSV export
└── pos-api/          # Main app, Flyway migrations, OpenAPI config
```

---

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker & docker-compose (for local database)

---

## Quick Start

### 1. Start PostgreSQL

```bash
docker-compose up db -d
```

### 2. Build

```bash
mvn clean package -DskipTests
```

### 3. Run

```bash
# Option A — run the JAR directly
java -DJWT_SECRET=your_secret_key_min_32_chars_long \
     -jar pos-api/target/pos-api-1.0.0.jar

# Option B — full Docker stack
docker-compose up --build
```

### 4. Verify

```
http://localhost:8080/actuator/health
http://localhost:8080/swagger-ui.html
```

---

## Default Admin Account

Seeded by `V2__seed_admin.sql`:

| Field | Value |
|-------|-------|
| Username | `admin` |
| Password | `admin123` |
| Role | `ADMIN` |

> **Change the password** immediately in production via `PUT /api/users/{id}`.

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `JWT_SECRET` | *(required)* | HS256 signing key, min 32 characters |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/pos_db` | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `pos_user` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | `pos_pass` | DB password |

---

## API Overview

### Authentication

```
POST   /api/auth/login           # returns access + refresh tokens
POST   /api/auth/refresh         # exchange refresh token for new access token
POST   /api/auth/logout          # invalidate refresh token
```

**Login example:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}'
```

Use the returned `accessToken` as `Authorization: Bearer <token>` on all subsequent requests.

### Products & Categories

```
GET    /api/products?search=&page=&size=
GET    /api/products/{id}
GET    /api/products/barcode/{code}
POST   /api/products               # MANAGER+
PUT    /api/products/{id}          # MANAGER+
DELETE /api/products/{id}          # MANAGER+ (soft delete)

GET    /api/categories
GET    /api/categories/{id}
POST   /api/categories             # MANAGER+
```

### Inventory

```
GET    /api/inventory/{productId}
GET    /api/inventory/low-stock
PUT    /api/inventory/{productId}/adjust    # MANAGER+
```

Adjustment types: `IN`, `OUT`, `ADJUST` (set absolute qty).

### Orders

```
POST   /api/orders                          # open new order
GET    /api/orders/{id}
POST   /api/orders/{id}/items               # add item
DELETE /api/orders/{id}/items/{itemId}      # remove item
POST   /api/orders/{id}/discount            # apply order-level discount
POST   /api/orders/{id}/pay                 # process payment → PAID
POST   /api/orders/{id}/void               # MANAGER+ → VOIDED
POST   /api/orders/{id}/refund             # MANAGER+ → REFUNDED
GET    /api/orders/{id}/receipt
GET    /api/orders/by-customer/{customerId}
```

**Order lifecycle:**
```
OPEN → (add items) → (discount) → pay → PAID
                                 → void (MANAGER) → VOIDED
PAID → refund (MANAGER) → REFUNDED
```

**Payment methods:** `CASH`, `CARD`, `QR`

### Customers

```
GET    /api/customers?search=&page=&size=
GET    /api/customers/{id}
POST   /api/customers
PUT    /api/customers/{id}
```

### Suppliers & Purchase Orders

```
GET    /api/suppliers
POST   /api/suppliers              # MANAGER+
GET    /api/purchase-orders
POST   /api/purchase-orders        # MANAGER+
GET    /api/purchase-orders/{id}
PUT    /api/purchase-orders/{id}/receive    # receive goods, updates stock
```

### Reports (MANAGER+)

```
GET    /api/reports/sales?from=2024-01-01&to=2024-01-31
GET    /api/reports/daily-close?date=2024-01-15
GET    /api/reports/top-products?limit=10
GET    /api/reports/cashier-summary?cashierId=&date=
GET    /api/reports/export?format=csv&from=&to=   # downloads CSV
```

### Users (ADMIN only)

```
GET    /api/users
GET    /api/users/{id}
POST   /api/users
DELETE /api/users/{id}    # deactivates user
```

---

## Role Matrix

| Endpoint Category | CASHIER | MANAGER | ADMIN |
|-------------------|:-------:|:-------:|:-----:|
| Login / Refresh | ✓ | ✓ | ✓ |
| View products / inventory | ✓ | ✓ | ✓ |
| Create / edit products | | ✓ | ✓ |
| Open order, add items, pay | ✓ | ✓ | ✓ |
| Void / refund order | | ✓ | ✓ |
| Stock adjustments | | ✓ | ✓ |
| Reports | | ✓ | ✓ |
| Suppliers / purchase orders | | ✓ | ✓ |
| User management | | | ✓ |

---

## Database Schema

13 tables managed by Flyway migrations:

```
users               refresh_tokens
categories          products            inventory
                    inventory_adjustments
customers
orders              order_items         payments
suppliers           purchase_orders     purchase_order_items
```

---

## API Response Format

All endpoints return a consistent envelope:

```json
{
  "success": true,
  "message": null,
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00"
}
```

Errors:
```json
{
  "success": false,
  "message": "Product not found with id: abc-123",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Running Without Docker

1. Create the database manually:
   ```sql
   CREATE USER pos_user WITH PASSWORD 'pos_pass';
   CREATE DATABASE pos_db OWNER pos_user;
   ```

2. Export the JWT secret:
   ```bash
   export JWT_SECRET=your_minimum_32_character_secret_key
   ```

3. Run:
   ```bash
   java -jar pos-api/target/pos-api-1.0.0.jar
   ```

Flyway runs automatically on startup and applies all migrations.

---

## Scheduled Jobs

| Job | Schedule | Description |
|-----|----------|-------------|
| `LowStockAlertJob` | Daily at 08:00 | Logs all products at or below `low_stock_threshold` |

---

## Module Dependency Graph

```
pos-common
    ├── pos-auth
    ├── pos-product
    │     └── pos-inventory
    └── pos-customer
          └── pos-order ── pos-auth, pos-product, pos-inventory, pos-customer, pos-payment
                └── pos-report ── pos-order, pos-payment, pos-inventory
          └── pos-supplier ── pos-auth, pos-product, pos-inventory
pos-api (aggregates all modules)
```

---

## Delivery Phases

- [x] **Phase 1** — Core: auth, product, inventory, order (cash), receipt
- [x] **Phase 2** — Commerce: customer, loyalty, discount, card/QR payment, refund/void, supplier, purchase orders
- [x] **Phase 3** — Ops: sales reports, daily close, top products, cashier summary, CSV export, low-stock scheduler
- [ ] **Phase 4** — Production: CI/CD pipeline, rate limiting, HTTPS, load testing, Kubernetes deployment
