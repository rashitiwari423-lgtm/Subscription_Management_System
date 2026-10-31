package com.subscriptiq.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationAnalyticsResponse {

    private int unusedResources;
    private BigDecimal potentialMonthlySavings;
    private BigDecimal potentialAnnualSavings;
    private int activeOpportunitiesCount;
    private List<OptimizationOpportunityDto> opportunities;
    private UtilizationSummaryDto utilizationSummary;
}
