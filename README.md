# Hotel Management System

Production-grade full-stack hotel management platform built with Spring Boot 3 / Java 21 (backend) and React 18 / Vite (frontend).

---

## Architecture Overview

```
hotel management system/
├── database/               MySQL 8.x DDL, stored procedures, seed data
├── hotel-backend/          Spring Boot 3.3 REST API (Java 21, Maven)
└── hotel-frontend/         React 18 SPA (Vite, Tailwind, Redux Toolkit)
```

### Backend Stack
| Layer | Technology |
|-------|------------|
| Runtime | Java 21, Spring Boot 3.3 |
| Security | Spring Security 6, JWT (RS256), Refresh Token Rotation |
| ORM | Hibernate 6, Spring Data JPA |
| Database | MySQL 8.x |
| Validation | Jakarta Bean Validation |
| Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| PDF | iTextPDF |
| Excel | Apache POI |
| Rate Limiting | Bucket4j |
| Testing | JUnit 5, Mockito, Testcontainers |

### Frontend Stack
| Layer | Technology |
|-------|------------|
| UI | React 18, Tailwind CSS |
| State | Redux Toolkit |
| Routing | React Router v6 |
| HTTP | Axios (with token-refresh interceptor) |
| Charts | Recharts |
| Forms | React Hook Form + Yup |
| Testing | Vitest + Testing Library |

---

## Prerequisites

- Java 21+
- Maven 3.9+
- MySQL 8.x
- Node.js 20+
- OpenSSL (for RSA key generation)

---

## Database Setup

```sql
CREATE DATABASE hotel_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'hotel_user'@'localhost' IDENTIFIED BY 'hotel_pass';
GRANT ALL PRIVILEGES ON hotel_db.* TO 'hotel_user'@'localhost';
FLUSH PRIVILEGES;

-- Then run in order:
mysql -u hotel_user -p hotel_db < database/schema.sql
mysql -u hotel_user -p hotel_db < database/stored_procedures.sql
mysql -u hotel_user -p hotel_db < database/seed_data.sql
```

---

## RSA Key Generation (JWT RS256)

```bash
cd hotel-backend/src/main/resources/keys
openssl genrsa -out private_raw.pem 2048
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in private_raw.pem -out private.pem
openssl rsa -in private_raw.pem -pubout -out public.pem
rm private_raw.pem
```

---

## Running the Backend

```bash
cd hotel-backend
export DB_USERNAME=hotel_user
export DB_PASSWORD=hotel_pass
mvn spring-boot:run
```

API available at: http://localhost:8080  
Swagger UI: http://localhost:8080/swagger-ui.html

Default staff credentials (from seed data):
- **Super Admin**: admin@grandhorizon.com / Password123!
- **Receptionist**: sarah.johnson@grandhorizon.com / Password123!
- **Housekeeping**: miguel.r@grandhorizon.com / Password123!

---

## Running the Frontend

```bash
cd hotel-frontend
npm install
npm run dev
```

App available at: http://localhost:5173

---

## Running Tests

```bash
# Backend unit + integration tests
cd hotel-backend
mvn test

# Frontend tests
cd hotel-frontend
npm test
```

---

## Key Architectural Decisions

### Double-Booking Prevention
The `reservations` and `rooms` tables use `@Version` (optimistic locking). Any concurrent write targeting the same row will trigger `ObjectOptimisticLockingFailureException`, which is caught by the global handler and returned as HTTP 409. The availability check uses a single JPQL query with a `NOT IN` subquery for atomic conflict detection.

### JWT Security (RS256)
Access tokens are short-lived (15 min), signed with an RSA private key. Refresh tokens are stored in the DB, one-time-use, and grouped into *families*. If a revoked token is presented (replay attack), the entire family is invalidated, forcing re-authentication on all devices.

### Idempotent Payments
Every payment request must carry an `X-Idempotency-Key` header. Duplicate keys are rejected with HTTP 409, preventing double charges on retried requests.

### Soft Deletes
All mutable entities inherit from `BaseEntity` with a `deleted_at` column. All queries filter `WHERE deleted_at IS NULL`. This preserves audit history and supports recovery.

### Optimistic UI (Frontend)
Room status changes apply `optimisticStatusUpdate` to the Redux store immediately, then dispatch the API call. On error, the server state is re-fetched and a toast error is shown. This keeps the UI snappy without needing WebSocket infrastructure for basic status updates.

---

## API Versioning

All endpoints are under `/api/v1/`. Error responses follow RFC 7807 `ProblemDetail` with a `type` URI, `title`, `detail`, `status`, and `timestamp`.

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_USERNAME` | `hotel_user` | MySQL username |
| `DB_PASSWORD` | `hotel_pass` | MySQL password |
| `DB_URL` | `jdbc:mysql://localhost:3306/hotel_db` | JDBC URL |
