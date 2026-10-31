package com.subscriptiq.analytics.service;

import com.subscriptiq.analytics.dto.AnalyticsOverviewResponse;
import com.subscriptiq.analytics.dto.ChurnAnalyticsResponse;
import com.subscriptiq.analytics.dto.RevenueAnalyticsResponse;
import com.subscriptiq.analytics.repository.AnalyticsRepository;
import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.repository.InvoiceRepository;
import com.subscriptiq.optimization.repository.OptimizationInsightRepository;
import com.subscriptiq.payment.repository.PaymentRepository;
import com.subscriptiq.plan.repository.PlanRepository;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PlanRepository planRepository;
    @Mock
    private OptimizationInsightRepository optimizationInsightRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Test
    @DisplayName("Should return zero counts and amounts gracefully for empty database overview")
    void testOverviewEmptyDatabase() {
        when(customerRepository.count()).thenReturn(0L);
        when(analyticsRepository.countActiveCustomers()).thenReturn(0L);
        when(subscriptionRepository.count()).thenReturn(0L);
        when(analyticsRepository.countActiveSubscriptions()).thenReturn(0L);
        when(analyticsRepository.countExpiringSubscriptions(any())).thenReturn(0L);
        when(invoiceRepository.count()).thenReturn(0L);
        when(invoiceRepository.findByStatus(any())).thenReturn(Collections.emptyList());
        when(analyticsRepository.sumTotalBilledAmount()).thenReturn(BigDecimal.ZERO);
        when(analyticsRepository.sumTotalCollectedAmount()).thenReturn(BigDecimal.ZERO);
        when(optimizationInsightRepository.findAll()).thenReturn(Collections.emptyList());
        when(analyticsRepository.sumMonthlyRecurringRevenue()).thenReturn(BigDecimal.ZERO);

        AnalyticsOverviewResponse overview = analyticsService.getOverview();

        assertThat(overview.getTotalCustomers()).isEqualTo(0);
        assertThat(overview.getTotalSubscriptions()).isEqualTo(0);
        assertThat(overview.getTotalBilledAmount()).isEqualByComparingTo("0.00");
        assertThat(overview.getTotalCollectedAmount()).isEqualByComparingTo("0.00");
        assertThat(overview.getOutstandingAmount()).isEqualByComparingTo("0.00");
        assertThat(overview.getPotentialOptimizationSavings()).isEqualByComparingTo("0.00");
        assertThat(overview.getMonthlyRecurringRevenue()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Should throw BadRequestException when startDate is after endDate")
    void testInvalidDateRangeThrowsBadRequest() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 4, 1);

        assertThatThrownBy(() -> analyticsService.getRevenueAnalytics(start, end, null, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("startDate must not be after endDate");

        assertThatThrownBy(() -> analyticsService.getSubscriptionAnalytics(start, end))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("startDate must not be after endDate");

        assertThatThrownBy(() -> analyticsService.getCustomerAnalytics(start, end))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("startDate must not be after endDate");

        assertThatThrownBy(() -> analyticsService.getPaymentAnalytics(start, end))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("startDate must not be after endDate");

        assertThatThrownBy(() -> analyticsService.getChurnAnalytics(start, end))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("startDate must not be after endDate");
    }

    @Test
    @DisplayName("Should safely handle Month-over-Month calculation with zero prior revenue")
    void testMomCalculationSafeWithZeroPriorRevenue() {
        Invoice inv1 = Invoice.builder()
                .invoiceDate(LocalDate.of(2026, 1, 10))
                .totalAmount(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .build();
        Invoice inv2 = Invoice.builder()
                .invoiceDate(LocalDate.of(2026, 2, 10))
                .totalAmount(new BigDecimal("5000.00"))
                .paidAmount(new BigDecimal("5000.00"))
                .build();

        when(invoiceRepository.findAll()).thenReturn(List.of(inv1, inv2));

        RevenueAnalyticsResponse response = analyticsService.getRevenueAnalytics(null, null, null, null, null);

        assertThat(response.getRevenueByMonth()).hasSize(2);
        // Month 1 has no previous -> 0.0
        assertThat(response.getRevenueByMonth().get(0).getMomGrowthPercentage()).isEqualTo(0.0);
        // Month 2 has previous = 0, current = 5000 -> safely 100.0 without NaN/Infinity
        assertThat(response.getRevenueByMonth().get(1).getMomGrowthPercentage()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Should safely handle zero denominator in churn calculation")
    void testChurnZeroDenominator() {
        when(subscriptionRepository.findAll()).thenReturn(Collections.emptyList());
        when(planRepository.findAll()).thenReturn(Collections.emptyList());

        ChurnAnalyticsResponse churn = analyticsService.getChurnAnalytics(null, null);

        assertThat(churn.getChurnRate()).isEqualTo(0.0);
        assertThat(churn.getTotalChurned()).isEqualTo(0);
        assertThat(churn.getMethodology()).isEqualTo("HISTORICAL_SUBSCRIPTION_LIFECYCLE");
    }
}
