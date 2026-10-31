// ─── User Roles ───────────────────────────────────────────────────────────────
export const ROLES = {
  ADMIN: 'ADMIN',
  STAFF: 'STAFF',
  CUSTOMER: 'CUSTOMER',
}

// ─── Routes ────────────────────────────────────────────────────────────────────
export const ROUTES = {
  LOGIN: '/login',
  FORGOT_PASSWORD: '/forgot-password',
  RESET_PASSWORD: '/reset-password',
  ONBOARDING: '/onboarding',
  UNAUTHORIZED: '/unauthorized',

  // Admin
  ADMIN: '/admin',
  ADMIN_DASHBOARD: '/admin/dashboard',
  ADMIN_CUSTOMERS: '/admin/customers',
  ADMIN_CUSTOMERS_NEW: '/admin/customers/new',
  ADMIN_STAFF: '/admin/staff',
  ADMIN_STAFF_NEW: '/admin/staff/new',
  ADMIN_PRODUCTS: '/admin/products',
  ADMIN_PRODUCTS_NEW: '/admin/products/new',
  ADMIN_PLANS: '/admin/plans',
  ADMIN_PLANS_NEW: '/admin/plans/new',
  ADMIN_QUOTATIONS: '/admin/quotations',
  ADMIN_QUOTATIONS_NEW: '/admin/quotations/new',
  ADMIN_SUBSCRIPTIONS: '/admin/subscriptions',
  ADMIN_SUBSCRIPTIONS_NEW: '/admin/subscriptions/new',
  ADMIN_INVOICES: '/admin/invoices',
  ADMIN_PAYMENTS: '/admin/payments',
  ADMIN_PAYMENTS_NEW: '/admin/payments/new',
  ADMIN_DISCOUNTS: '/admin/discounts',
  ADMIN_DISCOUNTS_NEW: '/admin/discounts/new',
  ADMIN_TAXES: '/admin/taxes',
  ADMIN_BILLING_RUNS: '/admin/billing-runs',
  ADMIN_DUNNING: '/admin/dunning',
  ADMIN_AI_COMMAND_CENTER: '/admin/ai-command-center',
  ADMIN_COST_OPTIMIZER: '/admin/cost-optimizer',
  ADMIN_ANALYTICS: '/admin/analytics',
  ADMIN_NOTIFICATIONS: '/admin/notifications',
  ADMIN_SETTINGS: '/admin/settings',
  ADMIN_AUDIT_LOGS: '/admin/audit-logs',
  ADMIN_INTEGRATIONS: '/admin/integrations',
  ADMIN_DEVELOPER: '/admin/developer',

  // Customer Portal
  CUSTOMER: '/customer',
  CUSTOMER_DASHBOARD: '/customer/dashboard',
  CUSTOMER_SUBSCRIPTIONS: '/customer/subscriptions',
  CUSTOMER_INVOICES: '/customer/invoices',
  CUSTOMER_PROFILE: '/customer/profile',
}

// ─── Subscription Statuses ─────────────────────────────────────────────────────
export const SUBSCRIPTION_STATUS = {
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
  TRIAL: 'TRIAL',
  PAST_DUE: 'PAST_DUE',
  CANCELLED: 'CANCELLED',
  PAUSED: 'PAUSED',
  PENDING: 'PENDING',
}

// ─── Invoice Statuses ──────────────────────────────────────────────────────────
export const INVOICE_STATUS = {
  DRAFT: 'DRAFT',
  SENT: 'SENT',
  PAID: 'PAID',
  PARTIALLY_PAID: 'PARTIALLY_PAID',
  OVERDUE: 'OVERDUE',
  CANCELLED: 'CANCELLED',
  VOID: 'VOID',
}

// ─── Payment Statuses ──────────────────────────────────────────────────────────
export const PAYMENT_STATUS = {
  PENDING: 'PENDING',
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED',
  REFUNDED: 'REFUNDED',
  PARTIALLY_REFUNDED: 'PARTIALLY_REFUNDED',
}

// ─── Quotation Statuses ────────────────────────────────────────────────────────
export const QUOTATION_STATUS = {
  DRAFT: 'DRAFT',
  SENT: 'SENT',
  ACCEPTED: 'ACCEPTED',
  REJECTED: 'REJECTED',
  EXPIRED: 'EXPIRED',
  CONVERTED: 'CONVERTED',
}

// ─── Payment Methods ───────────────────────────────────────────────────────────
export const PAYMENT_METHOD = {
  BANK_TRANSFER: 'BANK_TRANSFER',
  UPI: 'UPI',
  CREDIT_CARD: 'CREDIT_CARD',
  DEBIT_CARD: 'DEBIT_CARD',
  CHEQUE: 'CHEQUE',
  NET_BANKING: 'NET_BANKING',
  RAZORPAY: 'RAZORPAY',
  CASH: 'CASH',
}

// ─── Billing Cycles ────────────────────────────────────────────────────────────
export const BILLING_CYCLE = {
  MONTHLY: 'MONTHLY',
  QUARTERLY: 'QUARTERLY',
  SEMI_ANNUAL: 'SEMI_ANNUAL',
  ANNUAL: 'ANNUAL',
  CUSTOM: 'CUSTOM',
}

// ─── Discount Types ────────────────────────────────────────────────────────────
export const DISCOUNT_TYPE = {
  PERCENTAGE: 'PERCENTAGE',
  FIXED: 'FIXED',
}

// ─── Plan Tiers ────────────────────────────────────────────────────────────────
export const PLAN_TIER = {
  BASIC: 'BASIC',
  STARTER: 'STARTER',
  GROWTH: 'GROWTH',
  PROFESSIONAL: 'PROFESSIONAL',
  ENTERPRISE_PRO: 'ENTERPRISE_PRO',
  CUSTOM: 'CUSTOM',
}

// ─── Notification Types ────────────────────────────────────────────────────────
export const NOTIFICATION_TYPE = {
  INFO: 'INFO',
  SUCCESS: 'SUCCESS',
  WARNING: 'WARNING',
  ERROR: 'ERROR',
  AI: 'AI',
}

// ─── Tax Types ─────────────────────────────────────────────────────────────────
export const TAX_TYPE = {
  GST: 'GST',
  IGST: 'IGST',
  CGST: 'CGST',
  SGST: 'SGST',
  CESS: 'CESS',
}

// ─── Pagination ────────────────────────────────────────────────────────────────
export const DEFAULT_PAGE_SIZE = 10
export const PAGE_SIZE_OPTIONS = [10, 20, 50, 100]

// ─── Date Formats ──────────────────────────────────────────────────────────────
export const DATE_FORMAT = 'DD MMM YYYY'
export const DATETIME_FORMAT = 'DD MMM YYYY, HH:MM'

// ─── Currency ─────────────────────────────────────────────────────────────────
export const CURRENCY = {
  CODE: 'INR',
  SYMBOL: '₹',
  LOCALE: 'en-IN',
}

// ─── App Meta ──────────────────────────────────────────────────────────────────
export const APP_NAME = 'SubscriptIQ'
export const APP_VERSION = '1.0.0'
export const COMPANY_NAME = 'SubscriptIQ Technologies Pvt. Ltd.'

// ─── Audit Log Actions ─────────────────────────────────────────────────────────
export const AUDIT_ACTION = {
  CREATE: 'CREATE',
  UPDATE: 'UPDATE',
  DELETE: 'DELETE',
  LOGIN: 'LOGIN',
  LOGOUT: 'LOGOUT',
  EXPORT: 'EXPORT',
  APPROVE: 'APPROVE',
  REJECT: 'REJECT',
  SEND: 'SEND',
  CANCEL: 'CANCEL',
}

// ─── Customer Status ───────────────────────────────────────────────────────────
export const CUSTOMER_STATUS = {
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
  CHURNED: 'CHURNED',
  PROSPECT: 'PROSPECT',
  BLOCKED: 'BLOCKED',
}

// ─── Product Category ──────────────────────────────────────────────────────────
export const PRODUCT_CATEGORY = {
  CRM: 'CRM',
  ERP: 'ERP',
  HR: 'HR',
  ANALYTICS: 'ANALYTICS',
  COMMUNICATION: 'COMMUNICATION',
  SECURITY: 'SECURITY',
  DEVOPS: 'DEVOPS',
  FINANCE: 'FINANCE',
}

// ─── Local Storage Keys ────────────────────────────────────────────────────────
export const STORAGE_KEYS = {
  USER: 'subscriptiq_user',
  TOKEN: 'subscriptiq_token',
  THEME: 'subscriptiq_theme',
  SIDEBAR_COLLAPSED: 'subscriptiq_sidebar_collapsed',
}
