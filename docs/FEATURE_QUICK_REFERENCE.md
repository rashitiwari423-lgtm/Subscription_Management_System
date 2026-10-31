# SUBSCRIPT IQ — FEATURE QUICK REFERENCE

A concise quick-reference matrix of every feature, its purpose, primary user role, backend API route, and implementation status based on the live codebase audit.

| # | Feature | What It Does | Main User | Backend API Endpoint | Status |
|---|---|---|---|---|---|
| 1 | **User Registration** | Creates new customer user accounts with BCrypt-hashed credentials | Public | `POST /api/auth/register` | **IMPLEMENTED** |
| 2 | **User Login & JWT** | Validates credentials and returns signed HMAC-SHA384 bearer token | All Roles | `POST /api/auth/login` | **IMPLEMENTED** |
| 3 | **Password Reset** | Generates 15-minute token and resets forgotten password | All Roles | `POST /api/auth/forgot-password`, `/reset-password` | **IMPLEMENTED** |
| 4 | **Admin Dashboard** | Real-time executive KPIs (MRR, Active Subs, Overdue Receivables, AI Alerts) | ADMIN | `GET /api/analytics/dashboard` | **IMPLEMENTED** |
| 5 | **Staff Operations Dashboard** | Operational task metrics (Customers, Quotes, Subs, Overdue Invoices) | STAFF | `GET /api/customers`, `/api/quotations`, `/api/subscriptions` | **IMPLEMENTED** |
| 6 | **Customer Portal** | Self-service view of client subscriptions, invoices, and billing history | CUSTOMER | `GET /api/customers/{id}/subscriptions`, `/invoices` | **IMPLEMENTED** |
| 7 | **Customer Directory** | Paginated customer master list with search, status filters, and company data | ADMIN, STAFF | `GET /api/customers` | **IMPLEMENTED** |
| 8 | **Customer Provisioning** | Creates and updates corporate customer accounts with GSTIN identifiers | ADMIN, STAFF | `POST /api/customers`, `PUT /api/customers/{id}` | **IMPLEMENTED** |
| 9 | **Staff Directory & Status** | Operational team account management and active/inactive toggle | ADMIN | `GET /api/admin/users`, `PATCH /api/admin/users/{id}/status` | **IMPLEMENTED** |
| 10 | **Staff Provisioning** | Creates internal STAFF accounts with default operational role | ADMIN | `POST /api/admin/staff` | **IMPLEMENTED** |
| 11 | **Product Catalog** | Manages software products and platform offerings | ADMIN, STAFF | `GET /api/products`, `POST /api/products` | **IMPLEMENTED** |
| 12 | **Product Variants / SKUs** | Manages product editions (Standard, Pro, Enterprise) under products | ADMIN, STAFF | `GET /api/products/{id}/variants`, `POST ...` | **IMPLEMENTED** |
| 13 | **Plan Management** | Configures pricing tiers, billing cycles (Monthly, Annual), and license quotas | ADMIN, STAFF | `GET /api/plans`, `POST /api/variants/{id}/plans` | **IMPLEMENTED** |
| 14 | **Discounts & Promotions** | Creates and manages percentage/fixed discount codes with expiry dates | ADMIN, STAFF | `GET /api/discounts`, `POST /api/discounts` | **IMPLEMENTED** |
| 15 | **Tax & GST Configuration** | Configures regional tax rates (GST 18%, SGST, CGST, IGST) | ADMIN, STAFF | `GET /api/taxes`, `POST /api/taxes` | **IMPLEMENTED** |
| 16 | **CPQ Quotation Builder** | Assembles multi-line software quotes with live subtotal/tax/discount calculations | ADMIN, STAFF | `POST /api/quotations`, `GET /api/quotations` | **IMPLEMENTED** |
| 17 | **Quotation Acceptance** | Lifecycle state transition for quotes (`DRAFT` → `SENT` → `ACCEPTED` / `REJECTED`) | ADMIN, STAFF | `PATCH /api/quotations/{id}/status` | **IMPLEMENTED** |
| 18 | **Quote-to-Subscription** | Converts accepted quotes into active recurring subscriptions in 1 click | ADMIN, STAFF | `POST /api/quotations/{id}/convert` | **IMPLEMENTED** |
| 19 | **Subscription Directory** | Paginated contract directory with search, renewal dates, and status filters | ADMIN, STAFF | `GET /api/subscriptions` | **IMPLEMENTED** |
| 20 | **Subscription Lifecycle** | Pauses, resumes, renews, or closes/terminates active subscriptions | ADMIN, STAFF | `PATCH /api/subscriptions/{id}/pause`, `/resume`, `/close` | **IMPLEMENTED** |
| 21 | **Tax Invoice Directory** | Paginated tax invoices with balances, due dates, and status filters | ADMIN, STAFF | `GET /api/invoices` | **IMPLEMENTED** |
| 22 | **Manual Invoice Generation** | Generates on-demand tax invoice for an active subscription | ADMIN, STAFF | `POST /api/subscriptions/{id}/invoice` | **IMPLEMENTED** |
| 23 | **Invoice Cancellation** | Cancels uncollected or invalid invoices | ADMIN, STAFF | `POST /api/invoices/{id}/cancel` | **IMPLEMENTED** |
| 24 | **Payment Recording** | Records cash, card, UPI, or bank transfer payments against invoices | ADMIN, STAFF | `POST /api/invoices/{id}/payments` | **IMPLEMENTED** |
| 25 | **Payment Ledger** | Paginated ledger of all recorded transactions and payment receipts | ADMIN, STAFF | `GET /api/payments` | **IMPLEMENTED** |
| 26 | **Automated Billing Scheduler** | Daily cron job (`0 0 1 * * *`) that generates period invoices automatically | System Cron | Background `RecurringBillingScheduler` | **IMPLEMENTED** |
| 27 | **Manual Billing Run** | On-demand admin trigger for recurring billing execution cycle | ADMIN | `POST /api/admin/billing/run` | **IMPLEMENTED** |
| 28 | **Overdue Invoice Processing**| Automatically transitions delinquent unpaid invoices to `OVERDUE` | System Cron | `OverdueInvoiceService.processOverdueInvoices()` | **IMPLEMENTED** |
| 29 | **Dunning Recovery Hub** | Configurable reminder rules and recovery case management | ADMIN | Local UI state (`/admin/dunning`) | **PARTIALLY IMPLEMENTED** |
| 30 | **Usage Ingestion & History**| Ingests metric-based consumption snapshots (seats, storage, calls) | ADMIN, STAFF | `POST /api/usage`, `GET /api/usage` | **IMPLEMENTED** |
| 31 | **Cost Optimization Analysis**| Analyzes usage to detect underutilized licenses and idle capacity | ADMIN, STAFF | `POST /api/optimization/analyze` | **IMPLEMENTED** |
| 32 | **Deterministic Savings** | Computes monthly and annual savings using exact plan license prices | ADMIN, STAFF | `GET /api/optimization/insights` | **IMPLEMENTED** |
| 33 | **Optimization Approval** | Administrative review and approval/rejection of cost recommendations | ADMIN | `POST /api/optimization/insights/{id}/approve` | **IMPLEMENTED** |
| 34 | **AI Natural Language Chat** | Answers business questions using safe regex classification & typed backend tools | ADMIN | `POST /api/ai/ask` | **IMPLEMENTED** |
| 35 | **Prompt Injection Defense** | Blocks prompt injection and malicious SQL strings before evaluation | ADMIN | Internal `AiIntentClassifierImpl.java` | **IMPLEMENTED** |
| 36 | **AI Insights Directory** | List of detected business anomalies with severity and revenue metrics | ADMIN | `GET /api/ai/insights` | **IMPLEMENTED** |
| 37 | **AI Action Preview** | Read-only evaluation of affected customers and revenue before execution | ADMIN | `POST /api/ai/actions/preview` | **IMPLEMENTED** |
| 38 | **AI Action Approval** | Formal administrative sign-off requirement before any AI action executes | ADMIN | `POST /api/ai/actions/{id}/approve` | **IMPLEMENTED** |
| 39 | **AI Action Execution** | Executes approved action via dedicated Java executor with idempotency | ADMIN | `POST /api/ai/actions/{id}/execute` | **IMPLEMENTED** |
| 40 | **AI Action Audit History** | Immutable cryptographic audit ledger of all executed AI business actions | ADMIN | `GET /api/ai/actions/history` | **IMPLEMENTED** |
| 41 | **Revenue Analytics** | Billed vs collected vs overdue revenue broken down by month and plan | ADMIN | `GET /api/analytics/revenue` | **IMPLEMENTED** |
| 42 | **Subscription Analytics** | Visual status breakdown and plan acquisition trends | ADMIN | `GET /api/analytics/subscriptions` | **IMPLEMENTED** |
| 43 | **Descriptive Churn Analytics**| Calculates historical churn rates from closed contracts | ADMIN | `GET /api/analytics/churn` | **IMPLEMENTED** |
| 44 | **Revenue Forecasting** | Deterministic 3 to 12 month baseline projection using moving averages | ADMIN | `GET /api/analytics/forecast` | **IMPLEMENTED** |
| 45 | **Plan Performance Analytics**| Revenue and subscriber acquisition performance per pricing plan | ADMIN | `GET /api/analytics/plans` | **IMPLEMENTED** |
| 46 | **Email Notifications** | Dispatches password reset emails (simulated in logs if no SMTP) | All Roles | Internal `EmailServiceImpl.java` | **PARTIALLY IMPLEMENTED** |
| 47 | **In-App Notification Center**| UI alert feed for platform notifications and dunning alerts | ADMIN | Local mock data in `/admin/notifications` | **MOCKED** |
| 48 | **Generic Audit Telemetry** | Displays SOC2 Type II audit logs for security mutations | ADMIN | Local mock data in `/admin/audit-logs` | **MOCKED** |
| 49 | **Module-Level Search** | Real-time search across customers, quotes, subscriptions, and products | ADMIN, STAFF | Query parameter `?search=` on individual endpoints | **IMPLEMENTED** |
| 50 | **Global Header Search** | Search bar shortcut (⌘K) in application header | ADMIN, STAFF | Static UI widget without global backend index | **FRONTEND ONLY** |
| 51 | **Organization Settings** | Multi-tab form for company legal info and security policies | ADMIN | Component state in `/admin/settings` | **FRONTEND ONLY** |
| 52 | **System Health Telemetry** | Verifies operational readiness of backend server and database | Public | `GET /api/health` | **IMPLEMENTED** |
| 53 | **Developer API Key Page** | Shows API keys and sample curl commands for billing primitives | ADMIN | Static UI in `/admin/developer` | **FRONTEND ONLY** |
| 54 | **Payment Rail Integrations** | Toggle status cards for Razorpay, Stripe, Zoho Books, Slack, HubSpot | ADMIN | Component state in `/admin/integrations` | **FRONTEND ONLY** |

