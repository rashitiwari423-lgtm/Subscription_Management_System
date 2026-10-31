package com.subscriptiq.ai.action.entity;

import com.subscriptiq.ai.action.enums.AiActionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import com.subscriptiq.ai.entity.AiInsight;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
        name = "ai_action_requests",
        indexes = {
                @Index(name = "idx_ai_action_insight_id", columnList = "insight_id"),
                @Index(name = "idx_ai_action_type", columnList = "action_type"),
                @Index(name = "idx_ai_action_status", columnList = "status"),
                @Index(name = "idx_ai_action_created_at", columnList = "created_at"),
                @Index(name = "idx_ai_action_idempotency_key", columnList = "idempotency_key")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiActionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insight_id", nullable = false)
    private AiInsight insight;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private AiActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AiActionStatus status;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

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

    @Column(name = "affected_resources_summary", columnDefinition = "TEXT")
    private String affectedResourcesSummary;

    @Column(columnDefinition = "TEXT")
    private String warnings;

    @Column(name = "requires_approval", nullable = false)
    @Builder.Default
    private Boolean requiresApproval = true;

    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "requested_by", length = 150)
    private String requestedBy;

    @Column(name = "approved_by", length = 150)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_by", length = 150)
    private String rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = AiActionStatus.PENDING_APPROVAL;
        }
        if (this.requiresApproval == null) {
            this.requiresApproval = true;
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

