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
public class RevenueAnalyticsResponse {

    private BigDecimal totalBilledRevenue;
    private BigDecimal totalPaidRevenue;
    private BigDecimal outstandingRevenue;
    private BigDecimal overdueRevenue;
    private Double revenueGrowthRate;
    private List<MonthlyRevenueDto> revenueByMonth;
    private List<PlanRevenueDto> revenueByPlan;
    private List<CustomerRevenueDto> revenueByCustomer;
}
