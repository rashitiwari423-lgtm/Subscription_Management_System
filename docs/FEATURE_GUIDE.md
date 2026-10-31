# SUBSCRIPT IQ — COMPLETE FEATURE EXPLANATION & PROJECT DOCUMENTATION AUDIT

---

## 1. Project Overview

**SubscriptIQ** is an enterprise-grade, AI-powered subscription intelligence and CPQ (Configure, Price, Quote) billing engine developed using a **Spring Boot 3.2.5** backend and a **React 18 / Vite / Tailwind CSS** frontend. It is architected specifically to handle complex B2B subscription lifecycles, automated recurring billing schedules, GST-compliant tax invoicing, deterministic resource cost optimization, and safe AI-driven business decision support.

Unlike conventional subscription billing systems that treat AI as an autonomous, unmonitored chatbot with direct write access to financial databases, SubscriptIQ follows a **Deterministic Calculation & Controlled Execution Architecture**. AI natural-language questions are strictly translated into predefined business intents, evaluated against deterministic calculation engines, and restricted from executing database mutations without explicit, multi-stage human-in-the-loop (Admin) approval and cryptographic audit logging.

---

## 2. Architecture

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                      REACT 18 SPA (Vite + Tailwind)                     │
│  - RBAC Route Protection (Admin, Staff, Customer)                       │
│  - Centralized Axios HTTP Client (/src/services/api.js)                 │
│  - JWT Bearer Interceptor & Automatic 401/403/500 State Handlers        │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │ HTTPS / REST (Port 8080)
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT 3.2.5 APPLICATION                        │
│  ┌───────────────────────────────┬───────────────────────────────────┐  │
│  │      Security & Filter Layer  │     Business Service Layer        │  │
│  │  - Rate Limiting Filter       │  - BillingCalculationService      │  │
│  │  - JwtAuthenticationFilter    │  - QuotationCalculationService    │  │
│  │  - ExceptionTranslationFilter │  - CostOptimizationService        │  │
│  │  - Method Security (@PreAuth) │  - RecurringBillingService        │  │
│  └───────────────────────────────┴───────────────────────────────────┘  │
│  ┌───────────────────────────────┬───────────────────────────────────┐  │
│  │     AI Command Safety Layer   │     Background Schedulers         │  │
│  │  - Prompt Injection Regex     │  - RecurringBillingScheduler      │  │
│  │  - Intent Classifier          │    (Cron: 0 0 1 * * *)            │  │
│  │  - Deterministic Fact Tools   │  - OverdueInvoiceService          │  │
│  │  - 3-Stage Action Lifecycle   │  - Idempotent Billing Processor   │  │
│  └───────────────────────────────┴───────────────────────────────────┘  │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │ JPA / Hibernate 6
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                 MYSQL 8 DATABASE (21 Relational Tables)                 │
│  - Flyway Migrations V1 through V9                                      │
│  - Strict Foreign Key Constraints & Financial Precision (DECIMAL(12,2)) │
│  - Write-Once Immutable AI Audit Logs                                   │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Technology Stack

### Backend
- **Framework:** Spring Boot 3.2.5, Java 17
- **Security:** Spring Security 6.2.4, Nimbus JOSE JWT / JJWT (`HS384` signing algorithm), BCrypt password hashing (strength 10)
- **Data & ORM:** Spring Data JPA, Hibernate 6, Flyway Database Migrations (V1 to V9)
- **Database:** MySQL 8
- **Documentation:** SpringDoc OpenAPI 2.5.0 / Swagger UI 3.0
- **Validation:** Jakarta Validation (`@Valid`, `@NotNull`, `@Size`, `@DecimalMin`, `@Pattern`)
- **Testing:** JUnit 5, Mockito, Spring Security Test (325 Automated Unit & Integration Tests)

### Frontend
- **Framework:** React 18.3.1, Vite 5.4.21
- **Styling:** Tailwind CSS, Material Theme Colors & CSS Variables
- **Icons & Visuals:** Lucide React, Recharts (ResponsiveContainer, BarChart, PieChart, LineChart)
- **Routing:** React Router DOM 6.22 with Role-Based Route Guards (`ProtectedRoute.jsx`)
- **HTTP Client:** Centralized Axios instance with request/response interceptors

---

## 4. User Roles

The system strictly enforces 3 distinct roles at both the UI routing layer and the Spring Security method authorization layer:

| Role | Description | Accessible Workspace | Method Authority in Backend |
|---|---|---|---|
| **ADMIN** | Executive platform administrator with complete authority over pricing, billing runs, staff provisioning, AI action approval/execution, and financial ledgers. | `/admin/*` | `hasRole('ADMIN')` |
| **STAFF** | Operations, sales CPQ, customer service, and billing desk executives who manage quotations, customers, payments, and view insights. Cannot execute billing runs, approve AI actions, or modify staff/taxes. | `/staff/*`, plus shared admin screens permitted by backend | `hasAnyRole('ADMIN', 'STAFF')` |
| **CUSTOMER** | External client organization representative restricted strictly to self-service views of their own subscriptions, invoices, payment history, and usage analytics. | `/customer/*` | `hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')` with data scoped by customer ID |

---

## 5. Complete Feature List & Category Inventory

| # | Category / Feature Name | Backend Support | Frontend Connected | Status |
|---|---|---|---|---|
| 1 | **Authentication & Security** | Full (`/api/auth/*`) | Full (`/login`, `/register`, etc.) | **IMPLEMENTED** |
| 2 | **Dashboard & Overview** | Full (`/api/analytics/dashboard`) | Full (`/admin/dashboard`) | **IMPLEMENTED** |
| 3 | **Customer Management** | Full (`/api/customers/*`) | Full (`/admin/customers/*`) | **IMPLEMENTED** |
| 4 | **Staff & RBAC** | Full (`/api/admin/users`, `/api/admin/staff`) | Full (`/admin/staff/*`) | **IMPLEMENTED** |
| 5 | **Product Management** | Full (`/api/products/*`) | Full (`/admin/products/*`) | **IMPLEMENTED** |
| 6 | **Product Variants / SKUs** | Full (`/api/products/{id}/variants/*`) | Full (`/admin/products/:id/variants`) | **IMPLEMENTED** |
| 7 | **Plan & Pricing Management** | Full (`/api/plans/*`, `/api/variants/{id}/plans`) | Full (`/admin/plans/*`) | **IMPLEMENTED** |
| 8 | **Discounts & Promotions** | Full (`/api/discounts/*`) | Full (`/admin/discounts/*`) | **IMPLEMENTED** |
| 9 | **Taxes & GST** | Full (`/api/taxes/*`) | Full (`/admin/taxes/*`) | **IMPLEMENTED** |
| 10 | **Quotations / CPQ Engine** | Full (`/api/quotations/*`) | Full (`/admin/quotations/*`) | **IMPLEMENTED** |
| 11 | **Subscription Lifecycle** | Full (`/api/subscriptions/*`) | Full (`/admin/subscriptions/*`) | **IMPLEMENTED** |
| 12 | **Billing Engine** | Full (`BillingCalculationService`) | Full (Server calculation display) | **IMPLEMENTED** |
| 13 | **Invoices & Billing Runs** | Full (`/api/invoices/*`) | Full (`/admin/invoices/*`) | **IMPLEMENTED** |
| 14 | **Payments & Ledger** | Full (`/api/payments/*`, `/api/invoices/{id}/payments`) | Full (`/admin/payments/*`) | **IMPLEMENTED** |
| 15 | **Recurring Billing Scheduler** | Full (`POST /api/admin/billing/run`, Cron) | Full (`/admin/billing-runs`) | **IMPLEMENTED** |
| 16 | **Dunning & Recovery** | Partial (`OverdueInvoiceService`, status transition) | Frontend-Only UI rules (`/admin/dunning`) | **PARTIALLY IMPLEMENTED** |
| 17 | **Usage Tracking** | Full (`/api/usage/*`) | Full (`/cost-optimizer/usage`) | **IMPLEMENTED** |
| 18 | **Cost Optimization Engine** | Full (`/api/optimization/*`) | Full (`/admin/cost-optimizer/*`) | **IMPLEMENTED** |
| 19 | **AI Command Center (NLP)** | Full (`POST /api/ai/ask`) | Full (`/admin/ai-command-center`) | **IMPLEMENTED** |
| 20 | **AI Insights Engine** | Full (`GET /api/ai/insights`) | Full (`/admin/ai-command-center/insights`) | **IMPLEMENTED** |
| 21 | **AI Recommendations** | Full (`AiRecommendedAction` enum & tools) | Full (`InsightDetails.jsx`) | **IMPLEMENTED** |
| 22 | **AI Take Action (Preview)** | Full (`POST /api/ai/actions/preview`) | Full (`ActionPreview.jsx`) | **IMPLEMENTED** |
| 23 | **Action Approval & Execution** | Full (`POST /api/ai/actions/{id}/approve`, `/execute`) | Full (`ActionPreview.jsx`) | **IMPLEMENTED** |
| 24 | **Analytics Overview** | Full (`GET /api/analytics/overview`) | Full (`FinancialAnalytics.jsx`) | **IMPLEMENTED** |
| 25 | **Revenue Analytics** | Full (`GET /api/analytics/revenue`) | Full (`RevenueAnalytics.jsx`) | **IMPLEMENTED** |
| 26 | **Customer Analytics** | Full (`GET /api/analytics/customers`) | Full (Shared analytics dashboard) | **IMPLEMENTED** |
| 27 | **Subscription Analytics** | Full (`GET /api/analytics/subscriptions`) | Full (`SubscriptionAnalytics.jsx`) | **IMPLEMENTED** |
| 28 | **Churn Analytics** | Full (`GET /api/analytics/churn`) | Full (`ChurnAnalytics.jsx`) | **IMPLEMENTED** |
| 29 | **Revenue Forecasting** | Full (`GET /api/analytics/forecast`) | Full (Integrated in FinancialAnalytics) | **IMPLEMENTED** |
| 30 | **Notifications System** | Email password reset only (`EmailService`) | UI alert center uses mock data | **PARTIALLY IMPLEMENTED** |
| 31 | **Audit & History** | Full for AI actions (`ai_action_audit_logs`) | Generic audit page is mocked | **PARTIALLY IMPLEMENTED** |
| 32 | **Search** | Module search (Customers, Quotes, Subs, Products) | Header search bar is UI-only | **PARTIALLY IMPLEMENTED** |
| 33 | **Organization Settings** | None (Backend has no settings table) | Frontend UI form only (`OrgSettingsPage.jsx`) | **FRONTEND ONLY** |
| 34 | **System Health Telemetry** | Full (`GET /api/health`, `/api/test/*`) | Full (`TopBar.jsx` telemetry badge) | **IMPLEMENTED** |
| 35 | **Developer API Keys & Webhooks** | None (Static token display) | Frontend UI only (`DeveloperPage.jsx`) | **FRONTEND ONLY** |
| 36 | **Third-Party Integrations** | None (No external gateway bindings) | Frontend UI toggle cards (`IntegrationsPage.jsx`) | **FRONTEND ONLY** |

---

## 6. Feature-by-Feature Detailed Explanation

### 1. Authentication & Security
- **Purpose:** Secure identity verification, role assignment, and session token generation for administrative staff, sales, and client users.
- **Who Can Use It:** Public for registration and login; all authenticated roles for password reset and profile viewing.
- **What User Can Do:** Register corporate account, log in with email/password, request password reset token via email, submit new password with token, logout.
- **Workflow:**
  ```text
  User Credentials (Email + Password)
          ↓
  POST /api/auth/login
          ↓
  BCrypt Password Verification
          ↓
  Nimbus HS384 Signed JWT Generation (role + userId claim)
          ↓
  Frontend AuthContext stores in localStorage
          ↓
  Axios Interceptor injects "Authorization: Bearer <token>"
  ```
- **Connection:**
  `LoginPage.jsx` → `authService.js` → `POST /api/auth/login` → `AuthController.java` → `AuthServiceImpl.java` → `UserRepository` → `users` table.

### 2. Dashboard & Overview
- **Purpose:** Unified single-pane-of-glass executive monitoring of MRR, active subscription headcount, collected cash, outstanding receivables, overdue invoices, and urgent AI insights.
- **Who Can Use It:** ADMIN (`/admin/dashboard`), STAFF (`/staff/dashboard`), CUSTOMER (`/customer/portal`).
- **What User Can Do:** View live KPIs, refresh real-time metrics, review top overdue invoices, see high-severity AI risk alerts, navigate directly to billing runs or cost optimizer.
- **Connection:**
  `AdminDashboard.jsx` → `analyticsService.js` + `invoiceService.js` + `aiService.js` → `GET /api/analytics/dashboard`, `GET /api/invoices?status=OVERDUE`, `GET /api/ai/insights?size=3` → `AnalyticsController`, `InvoiceController`, `AiCommandController`.

### 3. Customer Management
- **Purpose:** Centralized B2B corporate customer master data repository maintaining company identities, tax identifiers (GSTIN), billing addresses, contact personnel, and account statuses.
- **Who Can Use It:** ADMIN and STAFF. CUSTOMER can view their own profile.
- **What User Can Do:** Create customer, view paginated directory, search by company name/email, update company metadata, toggle customer status (`ACTIVE`, `INACTIVE`, `SUSPENDED`).
- **Connection:**
  `CustomerDirectory.jsx` → `customerService.js` → `GET /api/customers?search=&status=&page=&size=` → `CustomerController.java` → `CustomerServiceImpl.java` → `CustomerRepository` → `customers` table.

### 4. Staff & RBAC Management
- **Purpose:** Internal workforce provisioning and access control governance ensuring operational staff accounts are strictly compartmentalized from administrative privileges.
- **Who Can Use It:** ADMIN only.
- **What User Can Do:** Provision new staff members with pre-assigned `STAFF` authority, view all active operational accounts, toggle account status (`ACTIVE` vs `INACTIVE`).
- **Connection:**
  `StaffListing.jsx` / `AddStaff.jsx` → `staffService.js` → `GET /api/admin/users?size=200`, `POST /api/admin/staff`, `PATCH /api/admin/users/{id}/status` → `AdminUserController.java` → `UserServiceImpl.java` → `UserRepository` → `users` table.

### 5. Product Management
- **Purpose:** Catalog repository for SaaS software suites, platform offerings, and physical/digital service tiers.
- **Who Can Use It:** ADMIN and STAFF for creation/management; CUSTOMER can view active products.
- **What User Can Do:** Create product, view catalog, search by name/category, activate or deactivate products, edit product description.
- **Connection:**
  `ProductCatalog.jsx` → `productService.js` → `GET /api/products` → `ProductController.java` → `ProductServiceImpl.java` → `ProductRepository` → `products` table.

### 6. Product Variants / SKUs
- **Purpose:** Fine-grained product editions (e.g. Standard, Professional, Enterprise) under a parent product line.
- **Who Can Use It:** ADMIN and STAFF.
- **What User Can Do:** Add variants to an existing product, view variant list by product ID, update variant SKU codes and attributes, activate/deactivate variants.
- **Connection:**
  `ProductVariants.jsx` → `productService.js` → `GET /api/products/{productId}/variants`, `POST /api/products/{productId}/variants` → `ProductVariantController.java` → `ProductVariantServiceImpl.java` → `ProductVariantRepository` → `product_variants` table.

### 7. Plan & Pricing Management
- **Purpose:** Concrete commercial billing agreements linked to a product variant specifying pricing, billing cadence (MONTHLY, QUARTERLY, ANNUAL), license quantities, and trial parameters.
- **Who Can Use It:** ADMIN and STAFF (manage); CUSTOMER (view active plans).
- **What User Can Do:** Create plan, list all plans, view plan details, modify price/billing cycle, toggle status (`ACTIVE`, `INACTIVE`).
- **Connection:**
  `PlansListing.jsx` / `CreatePlan.jsx` → `planService.js` → `GET /api/plans`, `POST /api/variants/{variantId}/plans`, `PUT /api/plans/{id}` → `PlanController.java` → `PlanServiceImpl.java` → `PlanRepository` → `plans` table.

### 8. Discounts & Promotions
- **Purpose:** Configurable promotional discount codes supporting percentage-off or fixed-amount subtractions with validity windows and usage rules.
- **Who Can Use It:** ADMIN only for creation and status toggle; STAFF can list active discounts to apply during quotation creation.
- **What User Can Do:** Create discount rule, list discounts, view active discounts, toggle status (`ACTIVE`, `EXPIRED`, `DISABLED`).
- **Connection:**
  `DiscountsPage.jsx` / `CreateDiscount.jsx` → `discountService.js` → `GET /api/discounts`, `POST /api/discounts` → `DiscountController.java` → `DiscountServiceImpl.java` → `DiscountRepository` → `discounts` table.

### 9. Taxes & GST
- **Purpose:** Regulatory tax rate master (e.g. GST 18%, SGST 9%, CGST 9%, IGST 18%) automatically applied to invoices and quotations based on jurisdiction.
- **Who Can Use It:** ADMIN only for creation/updates; STAFF can view active taxes.
- **What User Can Do:** Define new tax rates, view tax schedule, toggle active status.
- **Connection:**
  `TaxesPage.jsx` / `CreateTax.jsx` → `taxService.js` → `GET /api/taxes`, `POST /api/taxes` → `TaxController.java` → `TaxServiceImpl.java` → `TaxRepository` → `taxes` table.

### 10. Quotations / CPQ Engine
- **Purpose:** Full sales Configure, Price, Quote (CPQ) lifecycle where sales staff assemble multi-line item software quotes, apply discounts and taxes, present for customer acceptance, and convert directly into live subscriptions.
- **Who Can Use It:** ADMIN and STAFF (create, send, convert); CUSTOMER (view assigned quotations).
- **What User Can Do:** Build quotation with multiple line items, calculate totals with tax/discount, submit quotation, change status (`DRAFT`, `SENT`, `ACCEPTED`, `REJECTED`), convert `ACCEPTED` quotation to an active subscription with a single click.
- **Workflow:**
  ```text
  Select Customer
         ↓
  Add Plan Line Items & Quantities
         ↓
  Server Calculates Subtotal + Tax + Discounts
         ↓
  Save as DRAFT / Transition to SENT
         ↓
  Customer or Sales sets status to ACCEPTED
         ↓
  POST /api/quotations/{id}/convert
         ↓
  Backend creates active Subscription + Subscribed Plans
  ```
- **Connection:**
  `QuotationListing.jsx` / `CreateQuotation.jsx` → `quotationService.js` → `GET /api/quotations`, `POST /api/quotations`, `POST /api/quotations/{id}/convert` → `QuotationController.java` → `QuotationServiceImpl.java` → `QuotationRepository` → `quotations` & `quotation_items` tables.

### 11. Subscription Lifecycle Management
- **Purpose:** Execution and state machine enforcement of ongoing B2B software contracts across their complete lifecycle.
- **Who Can Use It:** ADMIN and STAFF (all lifecycle transitions); CUSTOMER (read-only view of own subscriptions).
- **What User Can Do:** List subscriptions, view subscription details, pause active subscription, resume paused subscription, manually renew subscription, close/cancel subscription.
- **State Machine Transitions:**
  - `DRAFT` / `PENDING` → `ACTIVE` (upon quotation conversion or initial billing activation)
  - `ACTIVE` → `PAUSED` (suspends billing and usage allocation)
  - `PAUSED` → `ACTIVE` (resumes recurring billing)
  - `ACTIVE` → `EXPIRED` (when contract ends without renewal)
  - `ACTIVE` / `PAUSED` → `CLOSED` (terminated; terminal state)
- **Connection:**
  `SubscriptionLifecycle.jsx` / `SubscriptionDetails.jsx` → `subscriptionService.js` → `GET /api/subscriptions`, `PATCH /api/subscriptions/{id}/pause`, `PATCH /api/subscriptions/{id}/resume`, `PATCH /api/subscriptions/{id}/close`, `PATCH /api/subscriptions/{id}/renew` → `SubscriptionController.java` → `SubscriptionServiceImpl.java` → `SubscriptionRepository` → `subscriptions` table.

### 12. Billing Calculation Engine
- **Purpose:** Pure, deterministic financial calculation service eliminating floating-point rounding errors and guaranteeing exact consistency between quotations, invoices, taxes, and ledgers.
- **Who Can Use It:** Internal backend service invoked by Invoice and Quotation services.
- **Business Logic Rules:**
  - `Subtotal = Σ (Line Item Unit Price × Quantity)`
  - `Discount Amount = Configured Discount Percentage or Fixed Value`
  - `Taxable Base = Max(0, Subtotal - Discount Amount)`
  - `Tax Amount = Taxable Base × (Tax Rate % / 100)`
  - `Total Amount = Taxable Base + Tax Amount`
  - All operations use `BigDecimal` with `RoundingMode.HALF_UP` to 2 decimal places.
- **Connection:**
  `BillingCalculationServiceImpl.java` & `QuotationCalculationServiceImpl.java`.

### 13. Invoices & Billing Runs
- **Purpose:** Compliant tax invoice issuance, balance tracking, due date monitoring, and manual generation for ad-hoc or cycle billing.
- **Who Can Use It:** ADMIN and STAFF (generate, update status, cancel); CUSTOMER (view and download invoices).
- **What User Can Do:** List invoices with status filter (`ISSUED`, `PAID`, `PARTIALLY_PAID`, `OVERDUE`, `CANCELLED`), view full line-item tax invoice breakdown, generate manual invoice for an active subscription, mark invoice cancelled.
- **Connection:**
  `InvoicesBillingRuns.jsx` / `InvoiceDetails.jsx` → `invoiceService.js` → `GET /api/invoices`, `POST /api/subscriptions/{id}/invoice`, `POST /api/invoices/{id}/cancel` → `InvoiceController.java` → `InvoiceServiceImpl.java` → `InvoiceRepository` → `invoices` & `invoice_items` tables.

### 14. Payments & Settlement
- **Purpose:** Cash ledger recording and invoice balance settlement. Supports full settlements, partial payments, and multi-transaction histories.
- **Who Can Use It:** ADMIN and STAFF (record payments); CUSTOMER (view own payment receipts).
- **What User Can Do:** Record new payment against an outstanding invoice (selecting payment method: CREDIT_CARD, BANK_TRANSFER, UPI, CASH), view paginated payment ledger, inspect payment details.
- **Settlement Logic:**
  - When payment amount matches invoice balance: Invoice status becomes `PAID`.
  - When payment amount is less than invoice balance: Invoice status becomes `PARTIALLY_PAID`, and remaining balance is updated.
  - Overpayment exceeding invoice balance is rejected with HTTP 400.
- **Connection:**
  `PaymentsLedger.jsx` / `RecordPayment.jsx` → `paymentService.js` → `GET /api/payments`, `POST /api/invoices/{id}/payments` → `PaymentController.java` → `PaymentServiceImpl.java` → `PaymentRepository` → `payments` table.

### 15. Recurring Billing Scheduler
- **Purpose:** Fully automated batch execution that evaluates active contracts, checks billing dates, processes overdue transitions, and generates recurring period invoices without human intervention.
- **Who Can Use It:** Automated Cron Job (`0 0 1 * * *` - 1:00 AM daily); Manual on-demand trigger by ADMIN via UI (`POST /api/admin/billing/run`).
- **What Admin Can Do:** Run immediate recurring billing cycle from the `/admin/billing-runs` screen, view summary of evaluated subscriptions, created invoices, skipped duplicates, and overdue updates.
- **Idempotency & Duplicate Prevention:**
  - Checks if an invoice for the current `billingPeriodStart` and `billingPeriodEnd` already exists for the given subscription.
  - Skips generation if already invoiced (`ProcessResult.DUPLICATE_SKIPPED`).
  - Advances `nextBillingDate` upon successful invoice creation.
- **Connection:**
  `BillingRunPage.jsx` → `billingService.js` → `POST /api/admin/billing/run` → `AdminBillingController.java` → `RecurringBillingServiceImpl.java` → `SubscriptionBillingProcessor.java`.

### 16. Dunning & Overdue Recovery
- **Purpose:** Automatic detection and classification of delinquent invoices whose due date has passed without full payment.
- **Who Can Use It:** Backend automated process; ADMIN and STAFF view overdue invoices.
- **What System Does:**
  - `OverdueInvoiceServiceImpl` scans for all `ISSUED` and `PARTIALLY_PAID` invoices where `dueDate < Today` and `balanceAmount > 0`.
  - Atomically transitions their status to `OVERDUE`.
  - Overdue status immediately flows to the Admin Dashboard and AI Overdue Payment Tool.
- **Status:** **PARTIALLY IMPLEMENTED**. (Backend automatically identifies and flags overdue invoices. The advanced dunning rules editor and simulated multi-step email recovery in `/admin/dunning` run as a client-side simulation).

### 17. Usage Tracking
- **Purpose:** Ingests metric-based utilization snapshots (e.g. allocated user seats vs used seats, storage GB allocated vs consumed) linked to subscriptions and customers.
- **Who Can Use It:** ADMIN and STAFF (record and view); CUSTOMER (view own utilization).
- **What User Can Do:** Record resource usage snapshot, view historical usage records, view utilization percentage graphs (`Used / Allocated * 100`).
- **Connection:**
  `UsageAnalytics.jsx` → `usageService.js` → `GET /api/usage`, `POST /api/usage` → `UsageController.java` → `UsageServiceImpl.java` → `UsageRecordRepository` → `usage_records` table.

### 18. Cost Optimization Engine
- **Purpose:** Deterministic cost efficiency analysis identifying underutilized software licenses, idle resources, and calculating concrete monthly and annual savings opportunities.
- **Who Can Use It:** ADMIN and STAFF (analyze, preview, approve, reject); CUSTOMER (view own optimization insights).
- **What User Can Do:** Trigger on-demand usage optimization analysis, view list of generated optimization insights, preview cost savings breakdown, approve or reject recommendations.
- **Deterministic Savings Calculation:**
  ```text
  Unused Quantity = Allocated Quantity - Used Quantity
  If Utilization < 70%:
      Severity = (Utilization < 40%) ? CRITICAL : HIGH
      Potential Monthly Savings = Unused Quantity × Unit License Price
      Potential Annual Savings = Potential Monthly Savings × 12
  ```
  *Note:* AI does NOT invent numbers; pricing and quantities are strictly drawn from active plan records and live usage snapshots.
- **Connection:**
  `CostOptimizer.jsx` / `RecommendationDetails.jsx` → `optimizationService.js` → `POST /api/optimization/analyze`, `GET /api/optimization/insights`, `POST /api/optimization/insights/{id}/approve` → `OptimizationController.java` → `CostOptimizationServiceImpl.java` → `OptimizationInsightRepository` → `optimization_insights` table.

### 19. AI Command Center (NLP Query Processing)
- **Purpose:** Natural language interface allowing executives to ask plain English or Hinglish business questions about overdue cash, expiring contracts, company health, and cost reduction opportunities.
- **Who Can Use It:** ADMIN only (`hasRole('ADMIN')`).
- **What User Can Do:** Type natural language questions in chat UI, receive structured answers, see auto-detected business intent, view accompanying insight cards, click recommended actions.
- **Safety & Injection Defense:**
  - Query is pre-screened against malicious regex patterns: `drop table`, `system prompt`, `ignore all instructions`, `select * from`, `exec(`, etc.
  - If malicious pattern is matched, query is rejected immediately with intent `UNSUPPORTED_QUERY`.
  - Zero arbitrary SQL or shell commands can be executed.
- **Supported Business Intents:**
  1. `BUSINESS_OVERVIEW` ("business overview", "kya problem hai")
  2. `OVERDUE_PAYMENTS` ("show overdue payments", "unpaid invoices")
  3. `EXPIRING_SUBSCRIPTIONS` ("which subscriptions are expiring?")
  4. `LATE_PAYMENTS` ("which customers pay late?")
  5. `REVENUE_SUMMARY` ("total revenue", "collection summary")
  6. `UNUSED_RESOURCES` ("unused resources", "idle licenses")
  7. `OPTIMIZATION_SUMMARY` ("how much can we save?", "cut costs")
  8. `PLAN_PERFORMANCE` ("which plans are losing customers?")
  9. `SUBSCRIPTION_SUMMARY` ("how many subscriptions?")
- **Connection:**
  `AICommandCenter.jsx` → `aiService.js` → `POST /api/ai/ask` → `AiCommandController.java` → `AiCommandServiceImpl.java` → `AiIntentClassifierImpl.java` → Controlled Backend Tool → `ai_query_logs` table.

### 20. AI Insights Engine
- **Purpose:** Persistent business intelligence records generated from background heuristics or AI command interactions, complete with severity levels and affected entity metrics.
- **Who Can Use It:** ADMIN only.
- **What User Can Do:** Filter insights by severity (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`, `INFO`) and status (`NEW`, `VIEWED`, `ACTION_PENDING`, `RESOLVED`), inspect detailed breakdown of affected customers, potential revenue, or potential savings.
- **Connection:**
  `InsightDetails.jsx` → `aiService.js` → `GET /api/ai/insights`, `PATCH /api/ai/insights/{id}/status` → `AiCommandController.java` → `AiInsightServiceImpl.java` → `AiInsightRepository` → `ai_insights` table.

### 21. AI Recommendations
- **Purpose:** Structured, type-safe recommended business actions paired with each insight (e.g. `FOLLOW_UP_OVERDUE_CUSTOMERS`, `LAUNCH_RENEWAL_CAMPAIGN`, `OPTIMIZE_UNUSED_LICENSES`, `REVIEW_LATE_PAYERS`).
- **Who Can Use It:** ADMIN only.
- **What User Can Do:** Click **"Take Action"** on any insight presenting a valid recommended action to open the multi-stage preview and approval flow.

### 22. AI Take Action (Preview Stage)
- **Purpose:** Stage 1 of Human-in-the-Loop AI execution. Evaluates the live database to determine exact affected customer counts, invoice IDs, and revenue amounts before any action is approved or taken.
- **Who Can Use It:** ADMIN only.
- **What User Can Do:** Trigger action preview, inspect affected line items and safety warnings, verify that no database mutations occur during preview.
- **Connection:**
  `ActionPreview.jsx` → `aiService.js` → `POST /api/ai/actions/preview` → `AiActionController.java` → `AiActionPreviewServiceImpl.java` → `ActionExecutor` → `ai_action_requests` table (status `PENDING_APPROVAL`).

### 23. Action Approval & Execution Stage
- **Purpose:** Stages 2 & 3 of Human-in-the-Loop AI execution. Enforces explicit administrative sign-off, strict idempotency keys, and dedicated transactional action executors.
- **Who Can Use It:** ADMIN only.
- **What User Can Do:** Explicitly approve action request (`POST /api/ai/actions/{id}/approve`), execute approved action (`POST /api/ai/actions/{id}/execute`), or reject action request (`POST /api/ai/actions/{id}/reject`). View immutable audit execution history.
- **Connection:**
  `ActionPreview.jsx` / `AIActionHistory.jsx` → `aiService.js` → `POST /api/ai/actions/{id}/approve`, `POST /api/ai/actions/{id}/execute`, `GET /api/ai/actions/history` → `AiActionController.java` → `AiActionExecutionServiceImpl.java` → `ai_action_executions` & `ai_action_audit_logs` tables.

### 24. Analytics Overview
- **Purpose:** Aggregated enterprise metrics across customer acquisition, active subscriptions, total invoiced amounts, and total collected cash.
- **Who Can Use It:** ADMIN only.
- **Connection:**
  `FinancialAnalytics.jsx` → `analyticsService.js` → `GET /api/analytics/overview` → `AnalyticsController.java` → `AnalyticsServiceImpl.java`.

### 25. Revenue Analytics
- **Purpose:** Billed vs collected vs outstanding revenue trends broken down by month, plan type, and customer accounts.
- **Who Can Use It:** ADMIN only.
- **Connection:**
  `RevenueAnalytics.jsx` → `analyticsService.js` → `GET /api/analytics/revenue` → `AnalyticsController.java` → `AnalyticsServiceImpl.java`.

### 26. Customer Analytics
- **Purpose:** Customer distribution, average revenue per user (ARPU), and customer lifetime value indications.
- **Who Can Use It:** ADMIN only.
- **Connection:**
  `AnalyticsController.java` → `GET /api/analytics/customers`.

### 27. Subscription Analytics
- **Purpose:** Visual distribution of subscriptions by status (Active, Paused, Expired, Closed), plan popularity breakdown, and churn risk metrics.
- **Who Can Use It:** ADMIN only.
- **Connection:**
  `SubscriptionAnalytics.jsx` → `analyticsService.js` → `GET /api/analytics/subscriptions` → `AnalyticsController.java` → `AnalyticsServiceImpl.java`.

### 28. Churn Analytics
- **Purpose:** Descriptive churn analytics calculated from closed and expired contracts against total active subscriber base over historical windows.
- **Who Can Use It:** ADMIN only.
- **Connection:**
  `ChurnAnalytics.jsx` → `analyticsService.js` → `GET /api/analytics/churn` → `AnalyticsController.java` → `AnalyticsServiceImpl.java`.

### 29. Revenue Forecasting
- **Purpose:** Deterministic baseline revenue projection for future 3 to 12 months using historical monthly moving averages and weighted trend models.
- **Who Can Use It:** ADMIN only.
- **Algorithm:**
  - `RECENT_MONTHLY_AVERAGE`: Computes mean revenue over preceding 3 to 6 calendar months and projects forward.
  - `WEIGHTED_RECENT_AVERAGE`: Applies higher weighting coefficients (e.g. 50%, 30%, 20%) to the most recent billing periods.
  - *No hallucinating LLM is involved; purely deterministic mathematical modeling.*
- **Connection:**
  `FinancialAnalytics.jsx` → `analyticsService.js` → `GET /api/analytics/forecast?months=3&methodology=RECENT_MONTHLY_AVERAGE` → `AnalyticsController.java` → `RevenueForecastServiceImpl.java`.

### 30. Notifications System
- **Current State:**
  - **Backend:** `EmailServiceImpl.java` implements `sendPasswordResetEmail()`. Dispatches via JavaMailSender if SMTP host is configured; logs simulation otherwise.
  - **Frontend:** `/admin/notifications` displays mock alert data from `mockData.js`.
- **Status:** **PARTIALLY IMPLEMENTED**.

### 31. Audit Logging
- **Current State:**
  - **Backend:** `AiActionAuditLog.java` records all AI action approvals, rejections, and executions in the `ai_action_audit_logs` table (Actor, Action, Affected Count, Result, Timestamp).
  - **Frontend:** `/admin/audit-logs` shows a static mock table representing SOC2 telemetry. AI action audit logs are accessible via `/admin/ai-command-center/history`.
- **Status:** **PARTIALLY IMPLEMENTED**.

### 32. Global Search
- **Current State:**
  - **Backend:** Module-level search is fully implemented in `CustomerController` (`?search=`), `QuotationController` (`?search=`), `SubscriptionController` (`?search=`), and `ProductController` (`?search=`).
  - **Frontend:** Header search bar in `TopBar.jsx` is a static UI shortcut (⌘K) without a cross-table global backend API.
- **Status:** **PARTIALLY IMPLEMENTED**.

### 33. Organization Settings
- **Current State:**
  - Dedicated screen `/admin/settings` exists with tabs for General, Security, and RBAC rules. Saves to local UI component state.
  - No backend settings table or REST controller exists.
- **Status:** **FRONTEND ONLY**.

### 34. System Health Telemetry
- **Purpose:** Verifies operational readiness of Spring Boot container and database connectivity.
- **Who Can Use It:** Public / Monitoring systems.
- **Endpoints:** `GET /api/health` returns `{"status":"UP","service":"SubscriptIQ Platform","version":"1.0.0"}`.
- **Connection:** `TopBar.jsx` telemetry badge reflects active connectivity.

### 35. Developer API Keys & Webhooks
- **Current State:**
  - Dedicated screen `/admin/developer` displays API keys, endpoints, and sample curl requests.
  - No dynamic API key generation or webhook dispatcher tables exist in backend.
- **Status:** **FRONTEND ONLY**.

### 36. Third-Party Integrations
- **Current State:**
  - Dedicated screen `/admin/integrations` provides toggle cards for Razorpay, Stripe, Zoho Books, Slack, QuickBooks, and HubSpot.
  - All status toggles run client-side. No real third-party webhooks or SDKs are linked.
- **Status:** **FRONTEND ONLY**.

---

## 7. Frontend → Backend → Database Mapping Reference

| Feature | Frontend Page | Frontend Service | HTTP Method & URL | Backend Controller | Backend Service | Database Table |
|---|---|---|---|---|---|---|
| Login | `LoginPage.jsx` | `authService.js` | `POST /api/auth/login` | `AuthController` | `AuthService` | `users` |
| Dashboard | `AdminDashboard.jsx` | `analyticsService.js` | `GET /api/analytics/dashboard` | `AnalyticsController` | `AnalyticsService` | `invoices`, `subscriptions`, `customers` |
| Customer List | `CustomerDirectory.jsx` | `customerService.js` | `GET /api/customers` | `CustomerController` | `CustomerService` | `customers` |
| Create Customer | `AddCustomer.jsx` | `customerService.js` | `POST /api/customers` | `CustomerController` | `CustomerService` | `customers` |
| Product Catalog | `ProductCatalog.jsx` | `productService.js` | `GET /api/products` | `ProductController` | `ProductService` | `products` |
| Plan List | `PlansListing.jsx` | `planService.js` | `GET /api/plans` | `PlanController` | `PlanService` | `plans` |
| Create Quotation | `CreateQuotation.jsx` | `quotationService.js` | `POST /api/quotations` | `QuotationController` | `QuotationService` | `quotations`, `quotation_items` |
| Convert Quotation | `QuotationDetails.jsx` | `quotationService.js` | `POST /api/quotations/{id}/convert` | `QuotationController` | `QuotationService` | `quotations`, `subscriptions` |
| Subscription List | `SubscriptionLifecycle.jsx` | `subscriptionService.js` | `GET /api/subscriptions` | `SubscriptionController` | `SubscriptionService` | `subscriptions` |
| Pause / Resume Sub | `SubscriptionDetails.jsx` | `subscriptionService.js` | `PATCH /api/subscriptions/{id}/pause` | `SubscriptionController` | `SubscriptionService` | `subscriptions` |
| Invoice List | `InvoicesBillingRuns.jsx` | `invoiceService.js` | `GET /api/invoices` | `InvoiceController` | `InvoiceService` | `invoices` |
| Record Payment | `RecordPayment.jsx` | `paymentService.js` | `POST /api/invoices/{id}/payments` | `PaymentController` | `PaymentService` | `payments`, `invoices` |
| Run Billing | `BillingRunPage.jsx` | `billingService.js` | `POST /api/admin/billing/run` | `AdminBillingController` | `RecurringBillingService` | `invoices`, `subscriptions` |
| Usage List | `UsageAnalytics.jsx` | `usageService.js` | `GET /api/usage` | `UsageController` | `UsageService` | `usage_records` |
| Cost Optimize | `CostOptimizer.jsx` | `optimizationService.js` | `POST /api/optimization/analyze` | `OptimizationController` | `CostOptimizationService` | `optimization_insights` |
| AI Ask Query | `AICommandCenter.jsx` | `aiService.js` | `POST /api/ai/ask` | `AiCommandController` | `AiCommandService` | `ai_query_logs`, `ai_insights` |
| AI Action Preview | `ActionPreview.jsx` | `aiService.js` | `POST /api/ai/actions/preview` | `AiActionController` | `AiActionPreviewService` | `ai_action_requests` |
| AI Action Approve | `ActionPreview.jsx` | `aiService.js` | `POST /api/ai/actions/{id}/approve` | `AiActionController` | `AiActionService` | `ai_action_requests` |
| AI Action Execute | `ActionPreview.jsx` | `aiService.js` | `POST /api/ai/actions/{id}/execute` | `AiActionController` | `AiActionExecutionService` | `ai_action_executions`, `ai_action_audit_logs` |
| Staff List | `StaffListing.jsx` | `staffService.js` | `GET /api/admin/users` | `AdminUserController` | `UserService` | `users` |
| Create Staff | `AddStaff.jsx` | `staffService.js` | `POST /api/admin/staff` | `AdminUserController` | `UserService` | `users` |

---

## 8. Complete API Inventory

### Auth Controller (`/api/auth`)
- `POST /api/auth/register` (Public) - Create new customer user account
- `POST /api/auth/login` (Public) - Authenticate and receive JWT
- `POST /api/auth/forgot-password` (Public) - Generate 15-minute password reset token
- `POST /api/auth/reset-password` (Public) - Reset password using verified token

### Customer Controller (`/api/customers`)
- `POST /api/customers` (ADMIN, STAFF) - Register corporate customer
- `GET /api/customers` (ADMIN, STAFF) - Paginated customer directory with search & status filters
- `GET /api/customers/{id}` (ADMIN, STAFF, CUSTOMER) - Get customer profile
- `PUT /api/customers/{id}` (ADMIN, STAFF) - Update customer details
- `PATCH /api/customers/{id}/status` (ADMIN, STAFF) - Activate/suspend customer

### Product & Variant Controllers (`/api/products`, `/api/variants`)
- `POST /api/products` (ADMIN, STAFF) - Create product
- `GET /api/products` (Authenticated) - List products
- `GET /api/products/{id}` (Authenticated) - Get product details
- `PUT /api/products/{id}` (ADMIN, STAFF) - Edit product
- `PATCH /api/products/{id}/status` (ADMIN, STAFF) - Activate/deactivate product
- `POST /api/products/{productId}/variants` (ADMIN, STAFF) - Create product variant
- `GET /api/products/{productId}/variants` (Authenticated) - List variants for product
- `GET /api/variants/{id}` (Authenticated) - Get variant details
- `PUT /api/variants/{id}` (ADMIN, STAFF) - Edit variant
- `PATCH /api/variants/{id}/status` (ADMIN, STAFF) - Toggle variant status

### Plan Controller (`/api/plans`)
- `POST /api/variants/{variantId}/plans` (ADMIN, STAFF) - Create billing plan
- `GET /api/plans` (Authenticated) - List all plans (paginated, status filter)
- `GET /api/variants/{variantId}/plans` (Authenticated) - List plans for variant
- `GET /api/plans/{id}` (Authenticated) - Get plan details
- `PUT /api/plans/{id}` (ADMIN, STAFF) - Update plan
- `PATCH /api/plans/{id}/status` (ADMIN, STAFF) - Activate/deactivate plan

### Quotation Controller (`/api/quotations`)
- `POST /api/quotations` (ADMIN, STAFF) - Create CPQ quote with multiple items
- `GET /api/quotations` (ADMIN, STAFF) - Paginated quote directory with search & status filters
- `GET /api/quotations/{id}` (ADMIN, STAFF, CUSTOMER) - View quotation details
- `PUT /api/quotations/{id}` (ADMIN, STAFF) - Modify quote items
- `PATCH /api/quotations/{id}/status` (ADMIN, STAFF) - Change status (SENT, ACCEPTED, REJECTED)
- `POST /api/quotations/{quotationId}/convert` (ADMIN, STAFF) - Convert ACCEPTED quote to active Subscription

### Subscription Controller (`/api/subscriptions`)
- `GET /api/subscriptions` (ADMIN, STAFF) - Paginated subscription directory
- `GET /api/subscriptions/{id}` (ADMIN, STAFF, CUSTOMER) - View subscription details
- `GET /api/customers/{customerId}/subscriptions` (ADMIN, STAFF, CUSTOMER) - View customer's subscriptions
- `PATCH /api/subscriptions/{id}/pause` (ADMIN, STAFF) - Pause active subscription
- `PATCH /api/subscriptions/{id}/resume` (ADMIN, STAFF) - Resume paused subscription
- `PATCH /api/subscriptions/{id}/close` (ADMIN, STAFF) - Cancel/terminate subscription
- `PATCH /api/subscriptions/{id}/renew` (ADMIN, STAFF) - Manually advance subscription renewal

### Invoice Controller (`/api/invoices`)
- `POST /api/subscriptions/{subscriptionId}/invoice` (ADMIN, STAFF) - Generate manual invoice
- `GET /api/invoices` (ADMIN, STAFF, CUSTOMER) - Paginated invoice directory (filter by status, customer)
- `GET /api/invoices/{id}` (ADMIN, STAFF, CUSTOMER) - Tax invoice detail view
- `PATCH /api/invoices/{id}/status` (ADMIN, STAFF) - Change invoice status
- `POST /api/invoices/{id}/cancel` (ADMIN, STAFF) - Cancel invoice
- `GET /api/customers/{customerId}/invoices` (ADMIN, STAFF, CUSTOMER) - Customer invoice list

### Payment Controller (`/api/payments`)
- `GET /api/payments` (ADMIN, STAFF) - Paginated ledger of all recorded payments
- `POST /api/invoices/{invoiceId}/payments` (ADMIN, STAFF) - Record payment against invoice
- `GET /api/invoices/{invoiceId}/payments` (ADMIN, STAFF, CUSTOMER) - Get invoice payment history
- `GET /api/payments/{id}` (ADMIN, STAFF, CUSTOMER) - Get payment receipt detail
- `GET /api/customers/{customerId}/payments` (ADMIN, STAFF, CUSTOMER) - Customer payment history

### Recurring Billing Controller (`/api/admin/billing`)
- `POST /api/admin/billing/run` (ADMIN only) - Trigger immediate recurring billing cycle

### Usage Controller (`/api/usage`)
- `POST /api/usage` (ADMIN, STAFF) - Record resource consumption snapshot
- `GET /api/usage` (ADMIN, STAFF) - List all usage records
- `GET /api/usage/{id}` (ADMIN, STAFF, CUSTOMER) - View single usage record
- `GET /api/usage/customer/{customerId}` (ADMIN, STAFF, CUSTOMER) - Customer usage history
- `GET /api/usage/subscription/{subscriptionId}` (ADMIN, STAFF, CUSTOMER) - Subscription usage history

### Cost Optimization Controller (`/api/optimization`)
- `POST /api/optimization/analyze` (ADMIN, STAFF) - Trigger deterministic usage optimization analysis
- `GET /api/optimization/insights` (ADMIN, STAFF) - List optimization recommendations
- `GET /api/optimization/insights/{id}` (ADMIN, STAFF, CUSTOMER) - Get optimization insight detail
- `POST /api/optimization/insights/{id}/preview` (ADMIN, STAFF) - Preview cost savings
- `POST /api/optimization/insights/{id}/approve` (ADMIN only) - Approve optimization recommendation
- `POST /api/optimization/insights/{id}/reject` (ADMIN only) - Reject optimization recommendation
- `GET /api/optimization/history` (ADMIN, STAFF) - View historical approved/rejected insights

### AI Command & Action Controllers (`/api/ai`, `/api/ai/actions`)
- `POST /api/ai/ask` (ADMIN only) - Submit natural language query
- `GET /api/ai/insights` (ADMIN only) - List detected AI insights
- `GET /api/ai/insights/{id}` (ADMIN only) - View AI insight details
- `PATCH /api/ai/insights/{id}/status` (ADMIN only) - Update insight lifecycle state
- `POST /api/ai/actions/preview` (ADMIN only) - Preview action impacts without mutating data
- `POST /api/ai/actions/{actionId}/approve` (ADMIN only) - Approve action for execution
- `POST /api/ai/actions/{actionId}/reject` (ADMIN only) - Reject action request
- `POST /api/ai/actions/{actionId}/execute` (ADMIN only) - Execute approved action
- `GET /api/ai/actions` (ADMIN only) - List action requests
- `GET /api/ai/actions/history` (ADMIN only) - View immutable execution audit log

### Analytics Controller (`/api/analytics`)
- `GET /api/analytics/overview` (ADMIN only) - High-level counts and totals
- `GET /api/analytics/revenue` (ADMIN only) - Billed, collected, overdue revenue breakdown
- `GET /api/analytics/subscriptions` (ADMIN only) - Subscription status distribution and plan breakdown
- `GET /api/analytics/customers` (ADMIN only) - Customer counts and growth
- `GET /api/analytics/payments` (ADMIN only) - Collection metrics and success rates
- `GET /api/analytics/churn` (ADMIN only) - Historical subscription churn metrics
- `GET /api/analytics/forecast` (ADMIN only) - Deterministic 3 to 12 month revenue forecast
- `GET /api/analytics/plans` (ADMIN only) - Performance metrics per pricing plan
- `GET /api/analytics/optimization` (ADMIN only) - Aggregate savings and idle resource totals
- `GET /api/analytics/dashboard` (ADMIN only) - Consolidated snapshot for dashboard KPIs

### Admin User Management Controller (`/api/admin`)
- `POST /api/admin/staff` (ADMIN only) - Provision internal STAFF user account
- `GET /api/admin/users` (ADMIN only) - Paginated listing of all registered users
- `GET /api/admin/users/{id}` (ADMIN only) - Get user profile
- `PATCH /api/admin/users/{id}/status` (ADMIN only) - Activate/deactivate user

---

## 9. Database Modules & Schema Design

The MySQL 8 database contains **21 relational tables** configured with strict foreign keys, indexing, and financial precision:

```text
users ◄── customers ◄── quotations ◄── quotation_items
  │           │              │
  │           ▼              ▼
  │      subscriptions ◄── plans ◄── product_variants ◄── products
  │           │              ▲
  │           ▼              │
  │        invoices ◄────────┴── taxes, discounts
  │           ▲
  │           ├── invoice_items
  │           └── payments
  │
  ├── usage_records ──► optimization_insights
  │
  └── ai_insights ──► ai_action_requests ──► ai_action_executions
                              │
                              └──► ai_action_audit_logs
```

### Table Inventory & Key Fields

1. `users`: System login credentials (`id`, `email` UNIQUE, `password` [BCrypt], `role` [ADMIN, STAFF, CUSTOMER], `status`).
2. `password_reset_tokens`: Transient reset secrets (`id`, `token`, `user_id` FK, `expiry_date`, `used`).
3. `customers`: Commercial account master (`id`, `company_name`, `email`, `phone`, `gstin`, `status`, `user_id` FK).
4. `products`: Software suites (`id`, `name`, `code` UNIQUE, `category`, `status`).
5. `product_variants`: Product editions (`id`, `product_id` FK, `name`, `sku` UNIQUE, `status`).
6. `plans`: Commercial pricing tiers (`id`, `variant_id` FK, `name`, `billing_cycle`, `price` DECIMAL(12,2), `status`).
7. `quotations`: CPQ quotes (`id`, `quotation_number` UNIQUE, `customer_id` FK, `status`, `subtotal`, `tax_amount`, `discount_amount`, `total_amount`, `valid_until`).
8. `quotation_items`: Quote line items (`id`, `quotation_id` FK, `plan_id` FK, `quantity`, `unit_price`, `total_price`).
9. `subscriptions`: Ongoing service contracts (`id`, `subscription_number` UNIQUE, `customer_id` FK, `status`, `start_date`, `end_date`, `next_billing_date`, `billing_cycle`, `mrr` DECIMAL(12,2)).
10. `taxes`: Tax schedule (`id`, `name`, `rate` DECIMAL(5,2), `tax_type`, `status`).
11. `discounts`: Promotional codes (`id`, `code` UNIQUE, `discount_type`, `value` DECIMAL(12,2), `status`).
12. `invoices`: Tax invoices (`id`, `invoice_number` UNIQUE, `subscription_id` FK, `customer_id` FK, `status`, `subtotal`, `tax_amount`, `discount_amount`, `total_amount`, `paid_amount`, `balance_amount`, `due_date`, `billing_period_start`, `billing_period_end`).
13. `invoice_items`: Invoice lines (`id`, `invoice_id` FK, `plan_id` FK, `description`, `quantity`, `unit_price`, `total_price`).
14. `payments`: Cash receipts (`id`, `payment_number` UNIQUE, `invoice_id` FK, `customer_id` FK, `amount` DECIMAL(12,2), `payment_method`, `status`, `payment_date`).
15. `usage_records`: Consumed units (`id`, `subscription_id` FK, `customer_id` FK, `resource_name`, `allocated_units`, `used_units`, `snapshot_date`).
16. `optimization_insights`: Cost reduction opportunities (`id`, `customer_id` FK, `subscription_id` FK, `resource_name`, `utilization_percentage`, `unused_units`, `potential_monthly_savings`, `severity`, `status`).
17. `ai_insights`: AI-detected anomalies & opportunities (`id`, `type`, `title`, `description`, `severity`, `affected_customers`, `affected_invoices`, `potential_revenue`, `potential_savings`, `recommended_action`, `status`).
18. `ai_query_logs`: Query telemetry & prompt auditing (`id`, `user_id`, `user_email`, `question`, `detected_intent`, `outcome`, `created_at`).
19. `ai_action_requests`: Action proposals (`id`, `insight_id` FK, `action_type`, `status` [PENDING_APPROVAL, APPROVED, REJECTED, EXECUTED, FAILED], `idempotency_key` UNIQUE, `preview_data`, `requested_by`).
20. `ai_action_executions`: Execution results (`id`, `action_id` FK, `action_type`, `executed_by`, `status`, `affected_count`, `result_summary`, `executed_at`).
21. `ai_action_audit_logs`: Immutable security ledger (`id`, `actor`, `action`, `action_type`, `action_id`, `result`, `affected_count`, `details`, `timestamp`).

---

## 10. AI Safety & Controlled Execution

SubscriptIQ implements an enterprise-grade defense-in-depth safety model:

1. **No Direct SQL Execution:** The AI service layer contains zero dynamic SQL builders or JDBC string concatenations. Queries cannot run `SELECT`, `UPDATE`, `INSERT`, or `DELETE`.
2. **Deterministic Fact Extraction:** When a question is received, the intent classifier matches the query against an enum of predefined business intents. An isolated Spring bean (e.g. `OverduePaymentTool`, `ExpiringSubscriptionTool`, `UnusedResourceTool`) queries the database using type-safe Spring Data JPA repositories.
3. **Prompt Injection Defense:** Regex filtering immediately traps attempts to bypass system prompts or extract database keys.
4. **Human-in-the-Loop 3-Stage Action Protocol:**
   - **Stage 1 (Preview):** The system evaluates live database state and generates an immutable preview DTO showing affected customers, dollar figures, and safety warnings. No database mutations occur. Status: `PENDING_APPROVAL`.
   - **Stage 2 (Approval):** An authenticated `ADMIN` must explicitly review and sign off. Status: `APPROVED`.
   - **Stage 3 (Execution):** Dedicated Java executors run typed logic (e.g., dispatching notices, generating renewal queues). Idempotency keys prevent double execution. Results are logged to the immutable `ai_action_audit_logs` table.

---

## 11. End-to-End Business Workflows

### CPQ Quotation to Subscription Conversion
```text
Sales Rep creates Quote
  └─ Selects Customer: "Nexus Dynamics"
  └─ Adds Line Item: "Enterprise Pro" × 5 units @ ₹12,000/mo
  └─ Applies GST: 18%
  └─ System calculates: Subtotal: ₹60,000 | Tax: ₹10,800 | Total: ₹70,800
  └─ Sets Status: SENT ➔ Customer reviews & sets ACCEPTED
  └─ Click "Convert to Subscription"
        ↓
QuotationController invokes QuotationService.convertToSubscription()
  └─ Quotation status changes to CONVERTED
  └─ Live Subscription created with status ACTIVE and nextBillingDate set
  └─ User redirected to newly provisioned Subscription
```

### Invoice Settlement Workflow
```text
Billing Run generates Invoice: INV-2024-001 (Total: ₹70,800, Due: 15 days)
  └─ Customer makes partial payment of ₹30,000 via Bank Transfer
  └─ Admin records payment in Payment Ledger
        ↓
PaymentService updates Invoice:
  └─ Paid Amount = ₹30,000
  └─ Balance Amount = ₹40,800
  └─ Status changes to PARTIALLY_PAID
        ↓
Customer makes second payment of ₹40,800 via UPI
        ↓
PaymentService updates Invoice:
  └─ Paid Amount = ₹70,800
  └─ Balance Amount = ₹0
  └─ Status changes to PAID
```

---

## 12. Recommended 5-Minute Live Demo Flow

1. **0:00 - 0:45 | Login & Executive Dashboard**
   - Log in at `http://localhost:5173` as `admin@subscriptiq.com` / `Password@123`.
   - Highlight live KPI cards (Revenue Collected, Active Subscriptions, Overdue Receivables).
   - Point out that data is fetched in real time from Spring Boot and MySQL.
2. **0:45 - 1:45 | CPQ Flow: Quotation to Subscription**
   - Navigate to **Quotations** (`/admin/quotations`). Click **"New Quotation"**.
   - Select Customer `Nexus Dynamics`, pick a Plan, enter quantity. Show automatic GST computation.
   - Save, mark as `ACCEPTED`, and click **"Convert to Subscription"**. Show instant transition to an active Subscription record.
3. **1:45 - 2:30 | Invoicing & Payment Settlement**
   - Navigate to **Invoices** (`/admin/invoices`). Open the generated invoice.
   - Navigate to **Payments** (`/admin/payments/new`). Record a partial payment against the invoice.
   - Show how the invoice status immediately updates to `PARTIALLY_PAID` with balance reduced.
4. **2:30 - 3:30 | Cost Optimization Engine**
   - Navigate to **Cost Optimizer** (`/admin/cost-optimizer`).
   - Click **"Analyze Resource Usage"**.
   - Show how underutilized licenses (< 70% utilization) generate deterministic cost savings recommendations without AI hallucinations.
5. **3:30 - 5:00 | AI Command Center & Controlled Take Action**
   - Open **AI Command Center** (`/admin/ai-command-center`).
   - Type query: *"Show overdue payments"* or *"What is my business health?"*.
   - Point out detected intent and structured insights.
   - Click **"Take Action"** ➔ Show the **Action Preview** with affected counts and safety warnings.
   - Click **"Approve Action"** ➔ Click **"Execute Action"**.
   - Open **History** (`/admin/ai-command-center/history`) to display the tamper-evident audit log.

---

## 13. Top Viva & Judge Questions Answered

### Q1: What problem does SubscriptIQ solve?
**Answer:** Traditional B2B SaaS companies struggle with fragmented systems for CPQ quoting, subscription lifecycle management, complex Indian GST compliance, and recurring billing. Furthermore, while companies want AI assistance, granting autonomous LLMs direct write access to financial databases creates catastrophic hallucination and security risks. SubscriptIQ unites CPQ, billing, and deterministic cost optimization while placing AI into a strictly controlled, human-in-the-loop decision-support boundary.

### Q2: How do you prevent the AI from making unsafe changes or hallucinating financial numbers?
**Answer:** The AI module does not generate or execute SQL. User questions are classified into strict enum intents by regex and keyword pattern classifiers. Pre-built Java service tools execute deterministic JPA queries against MySQL to compute factual counts and totals. When an AI recommendation is acted upon, it must pass through a 3-stage lifecycle: read-only Preview, Admin Approval, and Typed Executor Execution with idempotency keys and immutable audit logging.

### Q3: How is cost savings calculated in the Cost Optimizer?
**Answer:** Cost savings are purely deterministic. The system compares allocated units against used units from `usage_records`. If utilization falls below 70%, the unused quantity is multiplied by the plan's active unit price from the `plans` table (`Unused Units × Plan Price`). If utilization is under 40%, it is flagged as CRITICAL severity. AI never invents or estimates dollar savings.

### Q4: How does the system prevent duplicate billing during recurring billing runs?
**Answer:** The `SubscriptionBillingProcessor` checks if an invoice already exists for that subscription matching the exact `billingPeriodStart` and `billingPeriodEnd` dates. If found, it returns `ProcessResult.DUPLICATE_SKIPPED` and skips invoice generation. Once billed, the subscription's `nextBillingDate` is advanced according to its cadence (Monthly, Quarterly, Annual).

### Q5: How is security and RBAC enforced?
**Answer:** Security is enforced at both tiers. The React frontend uses `ProtectedRoute.jsx` to prevent unauthorized navigation, but the ultimate authority is Spring Security on the backend. Every controller method is guarded by `@PreAuthorize("hasRole('ADMIN')")` or `@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")`. Password hashing uses BCrypt with strength 10, and stateless JWT tokens are signed using HMAC-SHA384.

---

## 14. Known Limitations & Future Improvements

1. **In-App Notifications:** Currently, the notification center (`/admin/notifications`) is a frontend mock display. Future enhancement: implement WebSocket (STOMP) or Server-Sent Events (SSE) backed by a `notifications` database table.
2. **Third-Party Payment Gateways:** Razorpay and Stripe cards on `/admin/integrations` are UI simulations. Future enhancement: bind live webhooks and Razorpay AutoPay recurring payment mandates.
3. **Automated Dunning Communications:** While overdue invoices are automatically flagged by `OverdueInvoiceService`, automated dispatch of multi-stage SMS and WhatsApp reminders remains simulated on the frontend.
4. **Global Search Endpoint:** While each module has search filters, a unified search index across customers, invoices, and subscriptions via Elasticsearch or SQL union view would enhance executive navigation.

