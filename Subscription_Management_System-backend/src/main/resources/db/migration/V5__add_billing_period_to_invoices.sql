-- ========================================================
-- SubscriptIQ - Phase 6 Database Schema Migration
-- Module: Recurring Billing & Period Tracking on Invoices
-- ========================================================

ALTER TABLE invoices
    ADD COLUMN billing_period_start DATE NULL AFTER invoice_date,
    ADD COLUMN billing_period_end DATE NULL AFTER billing_period_start;

-- Add index and uniqueness constraint for subscription billing periods
ALTER TABLE invoices
    ADD CONSTRAINT uk_invoice_subscription_period UNIQUE (subscription_id, billing_period_start),
    ADD INDEX idx_invoice_billing_period (subscription_id, billing_period_start, billing_period_end);

