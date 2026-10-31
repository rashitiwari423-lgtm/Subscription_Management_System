package com.subscriptiq.subscription.entity;

import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.plan.entity.Plan;
import com.subscriptiq.quotation.entity.Quotation;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "subscriptions",
        indexes = {
                @Index(name = "idx_subscription_number", columnList = "subscription_number", unique = true),
                @Index(name = "idx_subscription_customer_id", columnList = "customer_id"),
                @Index(name = "idx_subscription_plan_id", columnList = "plan_id"),
                @Index(name = "idx_subscription_quotation_id", columnList = "quotation_id"),
                @Index(name = "idx_subscription_status", columnList = "status"),
                @Index(name = "idx_subscription_next_billing_date", columnList = "next_billing_date")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscription_number", nullable = false, unique = true, length = 50)
    private String subscriptionNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_subscription_customer")
    )
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "plan_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_subscription_plan")
    )
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "quotation_id",
            foreignKey = @ForeignKey(name = "fk_subscription_quotation")
    )
    private Quotation quotation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "next_billing_date", nullable = false)
    private LocalDate nextBillingDate;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = SubscriptionStatus.ACTIVE;
        }
        if (this.quantity == null || this.quantity <= 0) {
            this.quantity = 1;
        }
        if (this.discountAmount == null) {
            this.discountAmount = BigDecimal.ZERO;
        }
        if (this.taxAmount == null) {
            this.taxAmount = BigDecimal.ZERO;
        }
        if (this.totalAmount == null) {
            BigDecimal qty = BigDecimal.valueOf(this.quantity != null ? this.quantity : 1);
            BigDecimal price = this.unitPrice != null ? this.unitPrice : BigDecimal.ZERO;
            BigDecimal sub = price.multiply(qty);
            BigDecimal taxable = sub.subtract(this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO);
            if (taxable.compareTo(BigDecimal.ZERO) < 0) {
                taxable = BigDecimal.ZERO;
            }
            this.totalAmount = taxable.add(this.taxAmount != null ? this.taxAmount : BigDecimal.ZERO);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getCustomerId() {
        return customer != null ? customer.getId() : null;
    }

    public Long getPlanId() {
        return plan != null ? plan.getId() : null;
    }

    public Long getQuotationId() {
        return quotation != null ? quotation.getId() : null;
    }
}

