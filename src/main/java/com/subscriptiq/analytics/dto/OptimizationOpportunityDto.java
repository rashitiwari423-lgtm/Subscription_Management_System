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
public class OptimizationOpportunityDto {

    private Long insightId;
    private String resourceName;
    private String resourceType;
    private String customerName;
    private Integer unusedQuantity;
    private BigDecimal potentialMonthlySavings;
    private BigDecimal potentialAnnualSavings;
    private String recommendation;
    private String status;
}
