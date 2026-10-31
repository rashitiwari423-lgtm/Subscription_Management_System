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
public class PlanRevenueDto {

    private Long planId;
    private String planName;
    private String billingCycle;
    private BigDecimal billedRevenue;
    private int subscriptionCount;
}
