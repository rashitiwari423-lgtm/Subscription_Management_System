# SubscriptIQ — Complete API Documentation

This document provides a comprehensive inventory and specification of all REST API endpoints available in the **SubscriptIQ — AI Subscription Intelligence Platform**.

---

## 1. Authentication & Identity (`/api/auth`)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new user account | Public |
| `POST` | `/api/auth/login` | Authenticate user and issue JWT bearer token | Public (Rate-limited) |
| `POST` | `/api/auth/forgot-password` | Request password reset token via email | Public (Rate-limited) |
| `POST` | `/api/auth/reset-password` | Reset password using valid token | Public (Rate-limited) |

---

## 2. User Management (`/api/users` & `/api/admin/users`)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/test/me` | Retrieve current authenticated user profile | Authenticated |
| `POST` | `/api/admin/users/staff` | Create a new staff account | `ROLE_ADMIN` |
| `PATCH` | `/api/admin/users/{id}/status` | Activate or deactivate a user account | `ROLE_ADMIN` |

---

## 3. Customer Management (`/api/customers`)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/customers` | Create a new customer profile | `ADMIN`, `STAFF` |
| `GET` | `/api/customers` | List all customer profiles (paginated) | `ADMIN`, `STAFF` |
| `GET` | `/api/customers/{id}` | Get customer profile by ID | `ADMIN`, `STAFF`, Owner |
| `PUT` | `/api/customers/{id}` | Update customer profile | `ADMIN`, `STAFF` |
| `PATCH` | `/api/customers/{id}/status` | Update customer status (`ACTIVE`, `INACTIVE`, `SUSPENDED`) | `ADMIN`, `STAFF` |
| `GET` | `/api/customers/{id}/quotations` | List quotations for a customer | `ADMIN`, `STAFF`, Owner |
| `GET` | `/api/customers/{id}/subscriptions` | List subscriptions for a customer | `ADMIN`, `STAFF`, Owner |
| `GET` | `/api/customers/{id}/invoices` | List invoices for a customer | `ADMIN`, `STAFF`, Owner |
| `GET` | `/api/customers/{id}/payments` | List payments for a customer | `ADMIN`, `STAFF`, Owner |

---

## 4. Product Catalog (`/api/products`, `/api/variants`, `/api/plans`)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/products` | Create a new product | `ADMIN`, `STAFF` |
| `GET` | `/api/products` | List all products (paginated) | Authenticated |
| `GET` | `/api/products/{id}` | Get product by ID | Authenticated |
| `PUT` | `/api/products/{id}` | Update product details | `ADMIN`, `STAFF` |
| `PATCH` | `/api/products/{id}/status` | Update product status (`ACTIVE`, `INACTIVE`, `ARCHIVED`) | `ADMIN`, `STAFF` |
| `POST` | `/api/products/{productId}/variants` | Create a variant under a product | `ADMIN`, `STAFF` |
| `GET` | `/api/products/{productId}/variants` | List variants for a product | Authenticated |
| `GET` | `/api/variants/{id}` | Get variant by ID | Authenticated |
| `PUT` | `/api/variants/{id}` | Update product variant | `ADMIN`, `STAFF` |
| `PATCH` | `/api/variants/{id}/status` | Update variant status | `ADMIN`, `STAFF` |
| `POST` | `/api/variants/{variantId}/plans` | Create a subscription pricing plan | `ADMIN`, `STAFF` |
| `GET` | `/api/variants/{variantId}/plans` | List plans for a variant | Authenticated |
| `GET` | `/api/plans` | List all plans (paginated) | Authenticated |
| `GET` | `/api/plans/{id}` | Get plan by ID | Authenticated |
| `PUT` | `/api/plans/{id}` | Update subscription plan | `ADMIN`, `STAFF` |
| `PATCH` | `/api/plans/{id}/status` | Update plan status (`ACTIVE`, `INACTIVE`, `ARCHIVED`) | `ADMIN`, `STAFF` |

---

## 5. Quotations Lifecycle (`/api/quotations`)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/quotations` | Create a quotation with line items | `ADMIN`, `STAFF` |
| `GET` | `/api/quotations` | List all quotations (paginated) | `ADMIN`, `STAFF` |
| `GET` | `/api/quotations/{id}` | Get quotation by ID | `ADMIN`, `STAFF`, Owner |
| `PUT` | `/api/quotations/{id}` | Update a draft quotation | `ADMIN`, `STAFF` |
| `PATCH` | `/api/quotations/{id}/status` | Transition quotation status (`SENT`, `ACCEPTED`, `REJECTED`, `EXPIRED`) | `ADMIN`, `STAFF` |
| `POST` | `/api/quotations/{id}/convert` | Convert accepted quotation into an active subscription | `ADMIN`, `STAFF` |

---

## 6. Subscription Management (`/api/subscriptions`)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/subscriptions` | List all subscriptions (paginated) | `ADMIN`, `STAFF` |
| `GET` | `/api/subscriptions/{id}` | Get subscription by ID | `ADMIN`, `STAFF`, Owner |
| `PATCH` | `/api/subscriptions/{id}/pause` | Pause an active subscription | `ADMIN`, `STAFF` |
| `PATCH` | `/api/subscriptions/{id}/resume` | Resume a paused subscription | `ADMIN`, `STAFF` |
| `PATCH` | `/api/subscriptions/{id}/cancel` | Cancel an active or paused subscription | `ADMIN`, `STAFF` |
| `POST` | `/api/subscriptions/{id}/invoice` | Generate an on-demand invoice for a subscription | `ADMIN`, `STAFF` |

---

## 7. Tax & Discount Management (`/api/taxes`, `/api/discounts`)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/taxes` | Create a new tax rate | `ROLE_ADMIN` |
| `GET` | `/api/taxes` | List all taxes (paginated) | `ADMIN`, `STAFF` |
| `GET` | `/api/taxes/active` | List active taxes | `ADMIN`, `STAFF` |
| `GET` | `/api/taxes/{id}` | Get tax by ID | `ADMIN`, `STAFF` |
| `PUT` | `/api/taxes/{id}` | Update tax percentage and details | `ROLE_ADMIN` |
| `PATCH` | `/api/taxes/{id}/status` | Toggle tax status (`ACTIVE`, `INACTIVE`) | `ROLE_ADMIN` |
| `POST` | `/api/discounts` | Create a discount code | `ROLE_ADMIN` |
| `GET` | `/api/discounts` | List all discounts (paginated) | `ADMIN`, `STAFF` |
| `GET` | `/api/discounts/active` | List active discounts | `ADMIN`, `STAFF` |
| `GET` | `/api/discounts/{id}` | Get discount by ID | `ADMIN`, `STAFF` |
| `PUT` | `/api/discounts/{id}` | Update discount value and rules | `ROLE_ADMIN` |
| `PATCH` | `/api/discounts/{id}/status` | Toggle discount status (`ACTIVE`, `INACTIVE`) | `ROLE_ADMIN` |

---

## 8. Invoices & Billing (`/api/invoices`, `/api/admin/billing`)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/invoices` | List all invoices (paginated) | `ADMIN`, `STAFF` |
| `GET` | `/api/invoices/{id}` | Get invoice details by ID | `ADMIN`, `STAFF`, Owner |
| `PATCH` | `/api/invoices/{id}/void` | Void an open invoice | `ADMIN`, `STAFF` |
| `POST` | `/api/admin/billing/trigger` | Manually trigger recurring billing cycle | `ROLE_ADMIN` |

---

## 9. Payment Management (`/api/payments`)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/payments` | Record a payment against an invoice | `ADMIN`, `STAFF` |
| `GET` | `/api/payments` | List all payments (paginated) | `ADMIN`, `STAFF` |
| `GET` | `/api/payments/{id}` | Get payment by ID | `ADMIN`, `STAFF`, Owner |
| `GET` | `/api/invoices/{invoiceId}/payments` | List payments recorded for an invoice | `ADMIN`, `STAFF`, Owner |

---

## 10. Usage Tracking (`/api/usage`)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/usage` | Ingest resource usage snapshot | `ADMIN`, `STAFF` |
| `GET` | `/api/usage` | Query usage records (paginated, with date/customer/sub filters) | `ADMIN`, `STAFF` |
| `GET` | `/api/usage/{id}` | Get usage record by ID | `ADMIN`, `STAFF`, Owner |

---

## 11. Cost Optimization Engine (`/api/optimization`)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/optimization/analyze` | Analyze resource usage and generate cost optimization insights | `ROLE_ADMIN` |
| `GET` | `/api/optimization/insights` | List optimization insights (paginated) | `ADMIN`, `STAFF` |
| `GET` | `/api/optimization/insights/{id}` | Get optimization insight by ID | `ADMIN`, `STAFF`, Owner |
| `POST` | `/api/optimization/insights/{id}/preview` | Preview savings and impact of optimization | `ROLE_ADMIN` |
| `POST` | `/api/optimization/insights/{id}/approve` | Approve optimization recommendation | `ROLE_ADMIN` |
| `POST` | `/api/optimization/insights/{id}/reject` | Reject optimization recommendation | `ROLE_ADMIN` |
| `GET` | `/api/optimization/history` | Historical log of executed optimizations | `ROLE_ADMIN` |

---

## 12. AI Subscription Command Center (`/api/ai`)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/ai/ask` | Natural-language business question analysis | `ROLE_ADMIN` |
| `GET` | `/api/ai/insights` | List AI detected insights | `ROLE_ADMIN` |
| `GET` | `/api/ai/insights/{id}` | Get AI insight details | `ROLE_ADMIN` |
| `PATCH` | `/api/ai/insights/{id}/status` | Update insight review status | `ROLE_ADMIN` |

---

## 13. Controlled AI Business Actions (`/api/ai/actions`)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/ai/actions/preview` | Dry-run preview of whitelisted action with impact calculation | `ROLE_ADMIN` |
| `POST` | `/api/ai/actions/{id}/approve` | Approve an action request | `ROLE_ADMIN` |
| `POST` | `/api/ai/actions/{id}/reject` | Reject an action request with review notes | `ROLE_ADMIN` |
| `POST` | `/api/ai/actions/{id}/execute` | Idempotently execute approved action and record audit log | `ROLE_ADMIN` |
| `GET` | `/api/ai/actions` | List all action requests (paginated) | `ROLE_ADMIN` |
| `GET` | `/api/ai/actions/{id}` | Get action request, execution, and audit trail | `ROLE_ADMIN` |
| `GET` | `/api/ai/actions/history` | Historical audit trail of executed business actions | `ROLE_ADMIN` |

---

## 14. Analytics & Revenue Intelligence (`/api/analytics`)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/analytics/overview` | Platform-wide business overview counts & monetary totals | `ROLE_ADMIN` |
| `GET` | `/api/analytics/revenue` | Billed, collected, outstanding, overdue, and MoM growth | `ROLE_ADMIN` |
| `GET` | `/api/analytics/subscriptions` | Subscription status distribution and monthly acquisition | `ROLE_ADMIN` |
| `GET` | `/api/analytics/customers` | Customer acquisition, plan distribution, and average subs | `ROLE_ADMIN` |
| `GET` | `/api/analytics/payments` | Payment collection totals, success rates, and monthly trends | `ROLE_ADMIN` |
| `GET` | `/api/analytics/churn` | Descriptive historical churn rates by month and by plan | `ROLE_ADMIN` |
| `GET` | `/api/analytics/forecast` | Deterministic baseline revenue forecast (1 to 24 months) | `ROLE_ADMIN` |
| `GET` | `/api/analytics/plans` | Plan performance, revenue per plan, and plan churn | `ROLE_ADMIN` |
| `GET` | `/api/analytics/optimization` | Unused resource counts and potential monthly/annual savings | `ROLE_ADMIN` |
| `GET` | `/api/analytics/dashboard` | Consolidated lightweight snapshot across all modules | `ROLE_ADMIN` |

---

## 15. System Health & Documentation (`/api/health`, `/swagger-ui.html`, `/api-docs`)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/health` | Service health status check | Public |
| `GET` | `/swagger-ui.html` | Swagger UI interactive API explorer | Public |
| `GET` | `/api-docs` | OpenAPI 3.0 specification in JSON format | Public |
