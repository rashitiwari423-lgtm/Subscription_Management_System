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
public class AnalyticsOverviewResponse {

    private long totalCustomers;
    private long activeCustomers;
    private long totalSubscriptions;
    private long activeSubscriptions;
    private long expiringSubscriptions;
    private long totalInvoices;
    private long paidInvoices;
    private long overdueInvoices;
    private BigDecimal totalBilledAmount;
    private BigDecimal totalCollectedAmount;
    private BigDecimal outstandingAmount;
    private BigDecimal potentialOptimizationSavings;
    private BigDecimal monthlyRecurringRevenue;
}
