-- ========================================================
-- SubscriptIQ - Phase 7 Database Schema Migration
-- Modules: Usage Records, Cost Optimization Insights
-- ========================================================

CREATE TABLE IF NOT EXISTS usage_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    subscription_id BIGINT NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_name VARCHAR(255) NOT NULL,
    allocated_quantity INT NOT NULL DEFAULT 0,
    used_quantity INT NOT NULL DEFAULT 0,
    unit_cost DECIMAL(12, 2),
    snapshot_date DATE NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_usage_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_usage_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions (id) ON DELETE RESTRICT,
    INDEX idx_usage_customer_id (customer_id),
    INDEX idx_usage_subscription_id (subscription_id),
    INDEX idx_usage_snapshot_date (snapshot_date),
    INDEX idx_usage_resource_name (resource_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS optimization_insights (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    subscription_id BIGINT NOT NULL,
    usage_record_id BIGINT NOT NULL,
    resource_name VARCHAR(255) NOT NULL,
    allocated_quantity INT NOT NULL DEFAULT 0,
    used_quantity INT NOT NULL DEFAULT 0,
    unused_quantity INT NOT NULL DEFAULT 0,
    utilization_percentage DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    unit_cost DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    potential_monthly_savings DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    potential_annual_savings DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL,
    recommendation VARCHAR(30) NOT NULL,
    notes TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_opt_insight_usage_record UNIQUE (usage_record_id),
    CONSTRAINT fk_opt_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_opt_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions (id) ON DELETE RESTRICT,
    CONSTRAINT fk_opt_usage_record FOREIGN KEY (usage_record_id) REFERENCES usage_records (id) ON DELETE CASCADE,
    INDEX idx_opt_customer_id (customer_id),
    INDEX idx_opt_subscription_id (subscription_id),
    INDEX idx_opt_status (status),
    INDEX idx_opt_recommendation (recommendation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

