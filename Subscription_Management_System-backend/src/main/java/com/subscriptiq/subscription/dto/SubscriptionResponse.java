package com.subscriptiq.subscription.dto;

import com.subscriptiq.plan.enums.BillingCycle;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Subscription response payload")
public class SubscriptionResponse {

    @Schema(description = "Subscription ID", example = "1")
    private Long id;

    @Schema(description = "Unique subscription number", example = "SUB-20261031-1001")
    private String subscriptionNumber;

    @Schema(description = "Customer ID", example = "5")
    private Long customerId;

    @Schema(description = "Customer company name", example = "Acme Corp")
    private String customerCompanyName;

    @Schema(description = "Plan ID", example = "10")
    private Long planId;

    @Schema(description = "Plan name", example = "Enterprise Cloud Monthly")
    private String planName;

    @Schema(description = "Plan billing cycle", example = "MONTHLY")
    private BillingCycle billingCycle;

    @Schema(description = "Originating Quotation ID", example = "1")
    private Long quotationId;

    @Schema(description = "Subscription status", example = "ACTIVE")
    private SubscriptionStatus status;

    @Schema(description = "Start date", example = "2026-10-31")
    private LocalDate startDate;

    @Schema(description = "End date if applicable", example = "2027-10-31")
    private LocalDate endDate;

    @Schema(description = "Next recurring billing date", example = "2026-11-30")
    private LocalDate nextBillingDate;

    @Schema(description = "Quantity subscribed", example = "5")
    private Integer quantity;

    @Schema(description = "Snapshot unit price", example = "999.00")
    private BigDecimal unitPrice;

    @Schema(description = "Discount amount", example = "50.00")
    private BigDecimal discountAmount;

    @Schema(description = "Tax amount", example = "18.00")
    private BigDecimal taxAmount;

    @Schema(description = "Total recurring amount", example = "4963.00")
    private BigDecimal totalAmount;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}

