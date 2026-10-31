# SubscriptIQ — Production Readiness & Operations Guide

This guide details the operational requirements, security architecture, deployment procedures, and maintenance checklists for running **SubscriptIQ** in production environments.

---

## 1. Security Architecture & Checklist

- [x] **Stateless JWT Authentication**: Signed with HMAC-SHA256 (`Keys.hmacShaKeyFor`) using a minimum 256-bit cryptographically secure key.
- [x] **Role-Based Access Control (RBAC)**: Enforced via Spring Security HTTP filters and method-level `@PreAuthorize` annotations across all controller endpoints (`ROLE_ADMIN`, `ROLE_STAFF`, `ROLE_CUSTOMER`).
- [x] **Customer Tenant Isolation**: Enforced server-side in controllers and service methods; customers cannot access or query other customers' records.
- [x] **Rate Limiting**: In-memory sliding-window protection on sensitive authentication endpoints (`/api/auth/login`, `/api/auth/forgot-password`, `/api/auth/reset-password`).
- [x] **HTTP Security Headers**:
  - `X-Content-Type-Options: nosniff`
  - `X-Frame-Options: SAMEORIGIN` (prevents clickjacking)
  - `X-XSS-Protection: 0` (modern standard)
  - Strict Cache-Control on authenticated endpoints.
- [x] **Controlled AI Safety**: AI queries are strictly bounded to read-only tools and deterministic calculations. No arbitrary SQL generation, no direct DB mutations, and no unapproved executions.
- [x] **Audit Trail**: Every state transition on AI business actions writes immutable records to `ai_action_audit_logs`.
- [x] **Credential Protection**: Passwords are encrypted using BCrypt (`BCryptPasswordEncoder`). Passwords, JWT secrets, and reset tokens are excluded from all response DTOs and logging statements.

---

## 2. Environment Variables Specification

All sensitive credentials and environment-specific settings must be provided via external environment variables.

| Variable | Description | Required | Default / Example |
|---|---|---|---|
| `DB_HOST` | MySQL hostname or IP | Yes | `localhost` |
| `DB_PORT` | MySQL listening port | No | `3306` |
| `DB_NAME` | Database schema name | Yes | `subscriptiq` |
| `DB_USERNAME` | Database username | Yes | `subscriptiq_app` |
| `DB_PASSWORD` | Strong database password | Yes | *(Random secure secret)* |
| `DB_POOL_MAX` | HikariCP max pool size | No | `20` |
| `DB_POOL_MIN` | HikariCP min idle connections | No | `5` |
| `SERVER_PORT` | Application HTTP port | No | `8080` |
| `JWT_SECRET` | 256-bit (32-byte) secret key | Yes | *(Base64 or UTF-8 secure key)* |
| `JWT_EXPIRATION_MS` | Token lifespan in milliseconds | No | `86400000` (24 hours) |
| `CORS_ALLOWED_ORIGINS` | Comma-delimited list of origins | Yes in Prod | `https://app.subscriptiq.com` |
| `RATE_LIMIT_ENABLED` | Toggle auth rate limiting | No | `true` |
| `RATE_LIMIT_RPM` | Max requests per minute per IP | No | `120` |
| `BILLING_SCHEDULER_ENABLED` | Enable recurring billing job | No | `true` |
| `BILLING_SCHEDULER_CRON` | Cron expression for billing job | No | `0 0 1 * * *` (1 AM daily) |
| `INVOICE_DUE_DAYS` | Default days until invoice due | No | `7` |
| `MAIL_HOST` | SMTP server address | No (Prod Yes)| `smtp.mailgun.org` |
| `MAIL_PORT` | SMTP port | No | `587` |
| `MAIL_USERNAME` | SMTP username | No (Prod Yes)| `postmaster@domain` |
| `MAIL_PASSWORD` | SMTP password | No (Prod Yes)| *(Secret)* |
| `MAIL_FROM` | Sender email address | No | `noreply@subscriptiq.com` |

---

## 3. Database Migration & Schema Maintenance

- Database schema is managed strictly via **Flyway**.
- In production (`application-prod.yml`), `spring.flyway.clean-disabled` is set to `true` to prevent accidental drops.
- `spring.jpa.hibernate.ddl-auto` is set to `none`.
- Schema history is tracked in table `flyway_schema_history`.
- Pre-deployment checklist:
  1. Verify current database version: `SELECT MAX(version) FROM flyway_schema_history;`
  2. Backup database: `mysqldump -u <user> -p --single-transaction --quick subscriptiq > backup_$(date +%F).sql`
  3. Deploy application jar; Flyway will automatically apply pending migrations.

---

## 4. Recurring Billing Scheduler

- Identified subscriptions with `status = ACTIVE` and `nextBillingDate <= current_date` are automatically billed.
- Invoices are created with status `SENT` and idempotency constraints prevent duplicate invoices for the same subscription billing period (`uk_invoice_subscription_period`).
- The scheduler runs daily at 1:00 AM UTC (`0 0 1 * * *`).
- Manual billing triggers are restricted to `ROLE_ADMIN` via `POST /api/admin/billing/trigger`.

---

## 5. Deployment Procedures

### Standard Production Launch

```bash
# 1. Package production JAR
mvn clean package -DskipTests

# 2. Export environment variables
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST="prod-db.internal"
export DB_NAME="subscriptiq"
export DB_USERNAME="subscriptiq_app"
export DB_PASSWORD="<vault-password>"
export JWT_SECRET="<vault-jwt-secret-min-256-bits>"
export CORS_ALLOWED_ORIGINS="https://app.subscriptiq.com"

# 3. Launch application
java -jar -Dspring.profiles.active=prod target.nosync/subscriptiq-0.0.1-SNAPSHOT.jar
```

---

## 6. Health & Monitoring

- **Service Health**: `GET /api/health` returns `200 OK` with JSON payload:
  ```json
  {
    "success": true,
    "message": "Service is healthy and operational",
    "data": {
      "status": "UP",
      "service": "SubscriptIQ Platform",
      "version": "1.0.0"
    }
  }
  ```
- **Logging**:
  - Application logs: Set to `INFO` in production to prevent performance degradation.
  - Hibernate SQL and Spring Security debug logging are disabled in production.
  - Sensitive parameters (passwords, tokens) are omitted from log events.

---

## 7. Rollback Considerations

- Database migrations follow additive principles; rollback of migrations should be performed via compensating forward migrations whenever possible.
- If an emergency application rollback is needed:
  1. Re-deploy the previous application JAR.
  2. If a database rollback is strictly necessary, restore from the pre-deployment snapshot created prior to migration execution.
