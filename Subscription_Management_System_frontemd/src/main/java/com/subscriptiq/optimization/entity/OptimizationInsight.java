package com.subscriptiq.optimization.entity;

import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.optimization.enums.OptimizationRecommendation;
import com.subscriptiq.optimization.enums.OptimizationStatus;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.usage.entity.UsageRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
        name = "optimization_insights",
        indexes = {
                @Index(name = "idx_opt_customer_id", columnList = "customer_id"),
                @Index(name = "idx_opt_subscription_id", columnList = "subscription_id"),
                @Index(name = "idx_opt_status", columnList = "status"),
                @Index(name = "idx_opt_recommendation", columnList = "recommendation")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_opt_customer")
    )
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "subscription_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_opt_subscription")
    )
    private Subscription subscription;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "usage_record_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_opt_usage_record")
    )
    private UsageRecord usageRecord;

    @Column(name = "resource_name", nullable = false)
    private String resourceName;

    @Column(name = "allocated_quantity", nullable = false)
    @Builder.Default
    private Integer allocatedQuantity = 0;

    @Column(name = "used_quantity", nullable = false)
    @Builder.Default
    private Integer usedQuantity = 0;

    @Column(name = "unused_quantity", nullable = false)
    @Builder.Default
    private Integer unusedQuantity = 0;

    @Column(name = "utilization_percentage", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal utilizationPercentage = BigDecimal.ZERO;

    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "potential_monthly_savings", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal potentialMonthlySavings = BigDecimal.ZERO;

    @Column(name = "potential_annual_savings", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal potentialAnnualSavings = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OptimizationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OptimizationRecommendation recommendation;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = OptimizationStatus.DETECTED;
        }
        if (this.recommendation == null) {
            this.recommendation = OptimizationRecommendation.NO_ACTION;
        }
        if (this.unitCost == null) {
            this.unitCost = BigDecimal.ZERO;
        }
        if (this.potentialMonthlySavings == null) {
            this.potentialMonthlySavings = BigDecimal.ZERO;
        }
        if (this.potentialAnnualSavings == null) {
            this.potentialAnnualSavings = BigDecimal.ZERO;
        }
        if (this.utilizationPercentage == null) {
            this.utilizationPercentage = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

