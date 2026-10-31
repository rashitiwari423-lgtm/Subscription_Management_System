-- ==============================================================================
-- Flyway Migration: V9__add_analytics_indexes.sql
-- Description: Add analytics query performance indexes for date-range aggregations
-- ==============================================================================

CREATE INDEX idx_invoice_invoice_date ON invoices(invoice_date);
CREATE INDEX idx_subscription_start_date ON subscriptions(start_date);
CREATE INDEX idx_customer_created_at ON customers(created_at);
