-- Flyway Migration: V8__init_ai_action_schema.sql
-- SubscriptIQ Phase 9: Controlled AI Business Actions Schema

CREATE TABLE IF NOT EXISTS ai_action_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    insight_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    affected_customers INT NOT NULL DEFAULT 0,
    affected_subscriptions INT NOT NULL DEFAULT 0,
    affected_invoices INT NOT NULL DEFAULT 0,
    potential_revenue DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    potential_savings DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    affected_resources_summary TEXT,
    warnings TEXT,
    requires_approval BOOLEAN NOT NULL DEFAULT TRUE,
    idempotency_key VARCHAR(100) UNIQUE,
    requested_by VARCHAR(150),
    approved_by VARCHAR(150),
    approved_at DATETIME(6),
    rejected_by VARCHAR(150),
    rejected_at DATETIME(6),
    rejection_reason VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_ai_action_insight_id (insight_id),
    INDEX idx_ai_action_type (action_type),
    INDEX idx_ai_action_status (status),
    INDEX idx_ai_action_created_at (created_at),
    INDEX idx_ai_action_idempotency_key (idempotency_key),
    CONSTRAINT fk_ai_action_insight FOREIGN KEY (insight_id) REFERENCES ai_insights (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_action_executions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    executed_by VARCHAR(150) NOT NULL,
    status VARCHAR(30) NOT NULL,
    affected_count INT NOT NULL DEFAULT 0,
    result_summary TEXT,
    error_code VARCHAR(50),
    executed_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_ai_action_exec_action_id (action_id),
    INDEX idx_ai_action_exec_type (action_type),
    INDEX idx_ai_action_exec_status (status),
    INDEX idx_ai_action_exec_executed_at (executed_at),
    CONSTRAINT fk_ai_action_exec_action FOREIGN KEY (action_id) REFERENCES ai_action_requests (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_action_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor VARCHAR(150) NOT NULL,
    action VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_id BIGINT,
    insight_id BIGINT,
    result VARCHAR(50) NOT NULL,
    affected_count INT NOT NULL DEFAULT 0,
    details TEXT,
    created_at DATETIME(6) NOT NULL,
    INDEX idx_ai_audit_action_id (action_id),
    INDEX idx_ai_audit_insight_id (insight_id),
    INDEX idx_ai_audit_action_type (action_type),
    INDEX idx_ai_audit_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

