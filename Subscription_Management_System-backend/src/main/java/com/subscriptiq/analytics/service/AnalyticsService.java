package com.subscriptiq.analytics.service;

import com.subscriptiq.analytics.dto.AnalyticsOverviewResponse;
import com.subscriptiq.analytics.dto.ChurnAnalyticsResponse;
import com.subscriptiq.analytics.dto.CustomerAnalyticsResponse;
import com.subscriptiq.analytics.dto.DashboardSummaryResponse;
import com.subscriptiq.analytics.dto.OptimizationAnalyticsResponse;
import com.subscriptiq.analytics.dto.PaymentAnalyticsResponse;
import com.subscriptiq.analytics.dto.PlanPerformanceItemDto;
import com.subscriptiq.analytics.dto.RevenueAnalyticsResponse;
import com.subscriptiq.analytics.dto.SubscriptionAnalyticsResponse;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {

    AnalyticsOverviewResponse getOverview();

    RevenueAnalyticsResponse getRevenueAnalytics(LocalDate startDate, LocalDate endDate, String billingCycle, Long customerId, Long planId);

    SubscriptionAnalyticsResponse getSubscriptionAnalytics(LocalDate startDate, LocalDate endDate);

    CustomerAnalyticsResponse getCustomerAnalytics(LocalDate startDate, LocalDate endDate);

    PaymentAnalyticsResponse getPaymentAnalytics(LocalDate startDate, LocalDate endDate);

    ChurnAnalyticsResponse getChurnAnalytics(LocalDate startDate, LocalDate endDate);

    List<PlanPerformanceItemDto> getPlanPerformance(LocalDate startDate, LocalDate endDate);

    OptimizationAnalyticsResponse getOptimizationAnalytics();

    DashboardSummaryResponse getDashboardSummary();
}
