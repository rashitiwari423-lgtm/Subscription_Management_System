package com.subscriptiq.ai.entity;

import com.subscriptiq.ai.enums.AiInsightStatus;
import com.subscriptiq.ai.enums.AiRecommendedAction;
import com.subscriptiq.ai.enums.AiSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "ai_insights",
        indexes = {
                @Index(name = "idx_ai_insight_type", columnList = "type"),
                @Index(name = "idx_ai_insight_severity", columnList = "severity"),
                @Index(name = "idx_ai_insight_status", columnList = "status"),
                @Index(name = "idx_ai_insight_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AiSeverity severity;

    @Column(name = "affected_customers", nullable = false)
    @Builder.Default
    private Integer affectedCustomers = 0;

    @Column(name = "affected_subscriptions", nullable = false)
    @Builder.Default
    private Integer affectedSubscriptions = 0;

    @Column(name = "affected_invoices", nullable = false)
    @Builder.Default
    private Integer affectedInvoices = 0;

    @Column(name = "potential_revenue", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal potentialRevenue = BigDecimal.ZERO;

    @Column(name = "potential_savings", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal potentialSavings = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommended_action", length = 50)
    private AiRecommendedAction recommendedAction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AiInsightStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = AiInsightStatus.NEW;
        }
        if (this.severity == null) {
            this.severity = AiSeverity.INFO;
        }
        if (this.affectedCustomers == null) {
            this.affectedCustomers = 0;
        }
        if (this.affectedSubscriptions == null) {
            this.affectedSubscriptions = 0;
        }
        if (this.affectedInvoices == null) {
            this.affectedInvoices = 0;
        }
        if (this.potentialRevenue == null) {
            this.potentialRevenue = BigDecimal.ZERO;
        }
        if (this.potentialSavings == null) {
            this.potentialSavings = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

