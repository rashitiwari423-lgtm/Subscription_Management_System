package com.subscriptiq.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanPerformanceItemDto {

    private Long planId;
    private String planName;
    private String billingCycle;
    private BigDecimal price;
    private long totalSubscriptions;
    private long activeSubscriptions;
    private BigDecimal totalRevenue;
    private BigDecimal averageRevenuePerSubscription;
    private Double churnRate;
    private Double growthRate;
}
