# SubscriptIQ — AI Subscription Intelligence Platform

SubscriptIQ is an enterprise-grade subscription management and revenue intelligence backend engineered with **Spring Boot 3.2.5**, **Java 21**, and **MySQL 9**. It combines automated recurring billing, deterministic cost optimization, controlled AI business actions with admin approval workflows, and revenue intelligence analytics.

---

## 🚀 Key Capabilities

* **Authentication & RBAC**: Stateless JWT authentication with HMAC-SHA256, BCrypt password hashing, and role-based permissions (`ROLE_ADMIN`, `ROLE_STAFF`, `ROLE_CUSTOMER`).
* **Customer & Product Catalog**: Multi-tier catalog supporting products, variants, and flexible recurring billing cycles (Monthly, Quarterly, Annual).
* **Quotation → Subscription Lifecycle**: Draft, send, approve, and seamlessly convert quotations into active subscriptions with automated calculation of taxes and discounts.
* **Recurring Billing & Scheduler**: Spring Scheduler-driven recurring billing with strict period idempotency preventing duplicate invoicing.
* **Invoice & Payment Tracking**: Invoicing, payment reconciliation, partial payment support, and automated overdue tracking.
* **Usage Ingestion & Cost Optimizer**: Resource usage snapshot ingestion paired with deterministic underutilization detection and annual/monthly cost savings recommendations.
* **AI Command Center**: Natural-language business question analysis classifying intents and dispatching queries to read-only analytical tools.
* **Controlled AI Business Actions**: Human-in-the-loop AI actions featuring dry-run preview, dynamic server-side recalculation, admin approval gates, idempotent execution, and immutable audit logging.
* **Analytics & Revenue Intelligence**: Platform-wide overview, revenue breakdown with Month-over-Month (MoM) growth calculations, cohort acquisition, descriptive churn analytics, and baseline forecasting.

---

## 🛠 Technology Stack

* **Language**: Java 21 (LTS)
* **Framework**: Spring Boot 3.2.5
* **Security**: Spring Security 6.2, JJWT 0.12.5, BCrypt
* **Data Access**: Spring Data JPA, Hibernate 6.4
* **Database**: MySQL 8.0+ / 9.x
* **Database Migrations**: Flyway Community Edition
* **API Documentation**: SpringDoc OpenAPI 3.0 / Swagger UI
* **Testing**: JUnit 5, Mockito, AssertJ, Spring MockMvc

---

## 📋 Prerequisites

* **Java Development Kit (JDK)**: Version 21 or higher
* **Apache Maven**: Version 3.8+
* **MySQL Server**: Running on `localhost:3306` (or configured via environment)

---

## ⚙️ Environment Configuration

Copy `.env.example` to `.env` or set the environment variables in your environment:

```bash
cp .env.example .env
```

### Essential Environment Variables

```bash
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=subscriptiq
DB_USERNAME=root
DB_PASSWORD=password

# Security & JWT
JWT_SECRET=your_secret_key_at_least_256_bits_length_here
JWT_EXPIRATION_MS=86400000
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173,http://localhost:8080

# Recurring Billing Scheduler
BILLING_SCHEDULER_ENABLED=true
BILLING_SCHEDULER_CRON=0 0 1 * * *
INVOICE_DUE_DAYS=7
```

---

## 🏗 Database Setup & Migrations

SubscriptIQ manages its schema entirely via Flyway migrations located in `src/main/resources/db/migration`:

* `V1__init_auth_schema.sql` — Users and password reset tokens
* `V2__init_customer_product_plan_schema.sql` — Customers, products, variants, and plans
* `V3__init_quotation_subscription_schema.sql` — Quotations and subscriptions
* `V4__init_tax_discount_invoice_payment_schema.sql` — Taxes, discounts, invoices, items, and payments
* `V5__add_billing_period_to_invoices.sql` — Billing period constraints and idempotency index
* `V6__init_usage_and_optimization_schema.sql` — Usage records and optimization insights
* `V7__init_ai_insights_schema.sql` — AI insights and query logs
* `V8__init_ai_action_schema.sql` — AI action requests, executions, and audit logs
* `V9__add_analytics_indexes.sql` — Date-range performance indexes

Create the database in MySQL:

```sql
CREATE DATABASE IF NOT EXISTS subscriptiq CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

When the application starts, Flyway automatically applies all migrations.

---

## 🏃 Running the Application

### Local Development

```bash
mvn spring-boot:run
```

Or run the compiled JAR:

```bash
mvn clean package -DskipTests
java -jar target.nosync/subscriptiq-0.0.1-SNAPSHOT.jar
```

### Production Mode

```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar -Dspring.profiles.active=prod target.nosync/subscriptiq-0.0.1-SNAPSHOT.jar
```

---

## 🧪 Testing

Execute the complete regression test suite:

```bash
mvn clean test
```

All 325 unit and integration tests across Phases 1 through 11 will execute and verify database isolation, authentication, billing, optimization, AI safety, and analytics.

---

## 📖 API Documentation & Swagger

When the application is running, access Swagger UI and OpenAPI documentation at:

* **Swagger UI Explorer**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **OpenAPI 3.0 JSON Spec**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)
* **Health Check**: [http://localhost:8080/api/health](http://localhost:8080/api/health)

Detailed module specifications are available in [`docs/API_DOCUMENTATION.md`](docs/API_DOCUMENTATION.md).  
Production deployment guide is available in [`docs/PRODUCTION_READINESS.md`](docs/PRODUCTION_READINESS.md).

---

## 🔒 Security & AI Safety Architecture

* **Zero Arbitrary Execution**: External queries and AI models cannot execute arbitrary SQL, shell commands, or unapproved mutations.
* **Strict Whitelisting**: Every automated action (`SEND_RENEWAL_CAMPAIGN`, `FOLLOW_UP_OVERDUE_CUSTOMERS`, `REVIEW_UNUSED_RESOURCES`, `REVIEW_LATE_PAYMENTS`) is bound to an isolated, validated Java executor bean.
* **Admin Approval Gate**: AI recommendations cannot mutate system state directly. An explicit dry-run preview followed by administrator approval is required prior to execution.
* **Idempotency**: Duplicate executions return the previous cached result without duplicating side effects.
* **Full Audit Trail**: Every status change is written to `ai_action_audit_logs`.
