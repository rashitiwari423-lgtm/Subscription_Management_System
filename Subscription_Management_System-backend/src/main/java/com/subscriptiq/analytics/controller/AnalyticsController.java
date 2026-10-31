package com.subscriptiq.analytics.controller;

import com.subscriptiq.analytics.dto.AnalyticsOverviewResponse;
import com.subscriptiq.analytics.dto.ChurnAnalyticsResponse;
import com.subscriptiq.analytics.dto.CustomerAnalyticsResponse;
import com.subscriptiq.analytics.dto.DashboardSummaryResponse;
import com.subscriptiq.analytics.dto.OptimizationAnalyticsResponse;
import com.subscriptiq.analytics.dto.PaymentAnalyticsResponse;
import com.subscriptiq.analytics.dto.PlanPerformanceItemDto;
import com.subscriptiq.analytics.dto.RevenueAnalyticsResponse;
import com.subscriptiq.analytics.dto.RevenueForecastResponse;
import com.subscriptiq.analytics.dto.SubscriptionAnalyticsResponse;
import com.subscriptiq.analytics.service.AnalyticsService;
import com.subscriptiq.analytics.service.RevenueForecastService;
import com.subscriptiq.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics & Revenue Intelligence", description = "Endpoints for platform overview, revenue, subscriptions, churn, forecasting, and optimization analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final RevenueForecastService revenueForecastService;

    @GetMapping("/overview")
    @Operation(summary = "Get consolidated business overview analytics", description = "Returns high-level counts and totals across customers, subscriptions, invoices, and payments.")
    public ResponseEntity<ApiResponse<AnalyticsOverviewResponse>> getOverview() {
        log.info("Fetching business overview analytics");
        AnalyticsOverviewResponse response = analyticsService.getOverview();
        return ResponseEntity.ok(ApiResponse.success("Business overview retrieved successfully", response));
    }

    @GetMapping("/revenue")
    @Operation(summary = "Get revenue analytics", description = "Returns billed, collected, outstanding, and overdue revenue breakdown by month, plan, and customer.")
    public ResponseEntity<ApiResponse<RevenueAnalyticsResponse>> getRevenue(
            @Parameter(description = "Start date (ISO format YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO format YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Billing cycle filter (MONTHLY, QUARTERLY, ANNUAL, etc.)")
            @RequestParam(required = false) String billingCycle,
            @Parameter(description = "Customer ID filter")
            @RequestParam(required = false) Long customerId,
            @Parameter(description = "Plan ID filter")
            @RequestParam(required = false) Long planId
    ) {
        log.info("Fetching revenue analytics with startDate={}, endDate={}, billingCycle={}, customerId={}, planId={}",
                startDate, endDate, billingCycle, customerId, planId);
        RevenueAnalyticsResponse response = analyticsService.getRevenueAnalytics(startDate, endDate, billingCycle, customerId, planId);
        return ResponseEntity.ok(ApiResponse.success("Revenue analytics retrieved successfully", response));
    }

    @GetMapping("/subscriptions")
    @Operation(summary = "Get subscription lifecycle analytics", description = "Returns subscription status distribution, monthly acquisition, and plan breakdown.")
    public ResponseEntity<ApiResponse<SubscriptionAnalyticsResponse>> getSubscriptions(
            @Parameter(description = "Start date (ISO format YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO format YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Fetching subscription analytics with startDate={}, endDate={}", startDate, endDate);
        SubscriptionAnalyticsResponse response = analyticsService.getSubscriptionAnalytics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Subscription analytics retrieved successfully", response));
    }

    @GetMapping("/customers")
    @Operation(summary = "Get customer analytics", description = "Returns customer counts, monthly acquisition, plan distribution, and average subscriptions per customer.")
    public ResponseEntity<ApiResponse<CustomerAnalyticsResponse>> getCustomers(
            @Parameter(description = "Start date (ISO format YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO format YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Fetching customer analytics with startDate={}, endDate={}", startDate, endDate);
        CustomerAnalyticsResponse response = analyticsService.getCustomerAnalytics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Customer analytics retrieved successfully", response));
    }

    @GetMapping("/payments")
    @Operation(summary = "Get payment analytics", description = "Returns payment collection metrics, status counts, success rates, and monthly collections.")
    public ResponseEntity<ApiResponse<PaymentAnalyticsResponse>> getPayments(
            @Parameter(description = "Start date (ISO format YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO format YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Fetching payment analytics with startDate={}, endDate={}", startDate, endDate);
        PaymentAnalyticsResponse response = analyticsService.getPaymentAnalytics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Payment analytics retrieved successfully", response));
    }

    @GetMapping("/churn")
    @Operation(summary = "Get descriptive churn analytics", description = "Returns historical descriptive churn metrics calculated from actual subscription lifecycle data.")
    public ResponseEntity<ApiResponse<ChurnAnalyticsResponse>> getChurn(
            @Parameter(description = "Start date (ISO format YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO format YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Fetching churn analytics with startDate={}, endDate={}", startDate, endDate);
        ChurnAnalyticsResponse response = analyticsService.getChurnAnalytics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Churn analytics retrieved successfully", response));
    }

    @GetMapping("/forecast")
    @Operation(summary = "Get deterministic revenue baseline forecast", description = "Generates a deterministic baseline forecast based on historical monthly revenue.")
    public ResponseEntity<ApiResponse<RevenueForecastResponse>> getForecast(
            @Parameter(description = "Number of future months to project (1 to 24, default 3)")
            @RequestParam(defaultValue = "3") int months,
            @Parameter(description = "Forecasting methodology (RECENT_MONTHLY_AVERAGE, WEIGHTED_RECENT_AVERAGE)")
            @RequestParam(defaultValue = "RECENT_MONTHLY_AVERAGE") String methodology
    ) {
        log.info("Fetching revenue forecast for months={}, methodology={}", months, methodology);
        RevenueForecastResponse response = revenueForecastService.generateForecast(months, methodology);
        return ResponseEntity.ok(ApiResponse.success("Revenue forecast generated successfully", response));
    }

    @GetMapping("/plans")
    @Operation(summary = "Get plan performance analytics", description = "Returns subscription counts, active counts, revenue, and churn per plan.")
    public ResponseEntity<ApiResponse<List<PlanPerformanceItemDto>>> getPlans(
            @Parameter(description = "Start date (ISO format YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO format YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Fetching plan performance analytics with startDate={}, endDate={}", startDate, endDate);
        List<PlanPerformanceItemDto> response = analyticsService.getPlanPerformance(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Plan performance analytics retrieved successfully", response));
    }

    @GetMapping("/optimization")
    @Operation(summary = "Get cost optimization analytics", description = "Returns aggregate unused resources and potential monthly/annual savings calculated in Phase 7.")
    public ResponseEntity<ApiResponse<OptimizationAnalyticsResponse>> getOptimization() {
        log.info("Fetching optimization analytics");
        OptimizationAnalyticsResponse response = analyticsService.getOptimizationAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Optimization analytics retrieved successfully", response));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get consolidated dashboard summary", description = "Returns a lightweight dashboard-friendly snapshot of all analytics modules.")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboard() {
        log.info("Fetching consolidated dashboard summary");
        DashboardSummaryResponse response = analyticsService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success("Dashboard summary retrieved successfully", response));
    }
}
