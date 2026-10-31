-- ========================================================
-- SubscriptIQ - Phase 8 Database Schema Migration
-- Module: AI Subscription Command Center
-- Tables: ai_insights, ai_query_logs
-- ========================================================

CREATE TABLE IF NOT EXISTS ai_insights (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    severity VARCHAR(20) NOT NULL,
    affected_customers INT NOT NULL DEFAULT 0,
    affected_subscriptions INT NOT NULL DEFAULT 0,
    affected_invoices INT NOT NULL DEFAULT 0,
    potential_revenue DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    potential_savings DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    recommended_action VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_ai_insight_type (type),
    INDEX idx_ai_insight_severity (severity),
    INDEX idx_ai_insight_status (status),
    INDEX idx_ai_insight_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_query_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    user_email VARCHAR(150),
    question TEXT NOT NULL,
    detected_intent VARCHAR(50) NOT NULL,
    outcome VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_ai_query_user (user_id),
    INDEX idx_ai_query_intent (detected_intent),
    INDEX idx_ai_query_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

