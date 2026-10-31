package com.subscriptiq.plan.dto;

import com.subscriptiq.plan.entity.Plan;
import com.subscriptiq.plan.enums.BillingCycle;
import com.subscriptiq.plan.enums.PlanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {

    private Long id;
    private Long productVariantId;
    private String name;
    private String description;
    private BillingCycle billingCycle;
    private BigDecimal price;
    private String currency;
    private Integer trialDays;
    private PlanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PlanResponse fromEntity(Plan plan) {
        if (plan == null) {
            return null;
        }
        return PlanResponse.builder()
                .id(plan.getId())
                .productVariantId(plan.getProductVariantId())
                .name(plan.getName())
                .description(plan.getDescription())
                .billingCycle(plan.getBillingCycle())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .trialDays(plan.getTrialDays())
                .status(plan.getStatus())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}

