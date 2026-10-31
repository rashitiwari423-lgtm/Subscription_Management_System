package com.subscriptiq.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAnalyticsResponse {

    private long totalCustomers;
    private long activeCustomers;
    private long inactiveCustomers;
    private List<MonthlyCountDto> newCustomersByMonth;
    private List<PlanCustomerCountDto> customersByPlan;
    private Map<String, Long> customersBySubscriptionStatus;
    private Double averageSubscriptionsPerCustomer;
}
