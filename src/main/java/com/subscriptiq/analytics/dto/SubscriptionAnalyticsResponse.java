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
public class SubscriptionAnalyticsResponse {

    private long totalSubscriptions;
    private long activeSubscriptions;
    private long pendingSubscriptions;
    private long pausedSubscriptions;
    private long expiredSubscriptions;
    private long closedSubscriptions;
    private long draftSubscriptions;
    private List<MonthlyCountDto> newSubscriptionsByMonth;
    private List<MonthlyCountDto> cancellationsByMonth;
    private List<PlanSubscriptionCountDto> subscriptionsByPlan;
    private Map<String, Long> subscriptionsByBillingCycle;
}
