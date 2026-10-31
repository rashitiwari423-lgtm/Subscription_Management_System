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
public class DashboardSummaryResponse {

    private RevenueSnapshot revenue;
    private SubscriptionSnapshot subscriptions;
    private CustomerSnapshot customers;
    private PaymentSnapshot payments;
    private ChurnSnapshot churn;
    private OptimizationSnapshot optimization;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueSnapshot {
        private BigDecimal totalBilledRevenue;
        private BigDecimal totalPaidRevenue;
        private BigDecimal outstandingRevenue;
        private BigDecimal overdueRevenue;
        private Double revenueGrowthRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionSnapshot {
        private long totalSubscriptions;
        private long activeSubscriptions;
        private long pausedSubscriptions;
        private long expiredSubscriptions;
        private long closedSubscriptions;
        private BigDecimal monthlyRecurringRevenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSnapshot {
        private long totalCustomers;
        private long activeCustomers;
        private long inactiveCustomers;
        private Double averageSubscriptionsPerCustomer;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentSnapshot {
        private long totalPayments;
        private BigDecimal totalCollected;
        private long successfulPayments;
        private Double paymentSuccessRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChurnSnapshot {
        private Double churnRate;
        private long totalChurned;
        private String methodology;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizationSnapshot {
        private int unusedResources;
        private BigDecimal potentialMonthlySavings;
        private BigDecimal potentialAnnualSavings;
        private int activeOpportunitiesCount;
    }
}
