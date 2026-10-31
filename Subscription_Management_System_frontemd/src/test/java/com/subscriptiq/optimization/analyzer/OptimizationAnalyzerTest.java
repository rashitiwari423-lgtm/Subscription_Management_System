package com.subscriptiq.optimization.analyzer;

import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.optimization.entity.OptimizationInsight;
import com.subscriptiq.optimization.enums.OptimizationRecommendation;
import com.subscriptiq.optimization.enums.OptimizationStatus;
import com.subscriptiq.optimization.repository.OptimizationInsightRepository;
import com.subscriptiq.optimization.service.CostOptimizationService;
import com.subscriptiq.optimization.service.CostOptimizationServiceImpl;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.usage.entity.UsageRecord;
import com.subscriptiq.usage.service.UsageCalculationService;
import com.subscriptiq.usage.service.UsageCalculationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OptimizationAnalyzerTest {

    @Mock
    private OptimizationInsightRepository optimizationInsightRepository;

    private UsageCalculationService usageCalculationService;
    private CostOptimizationService costOptimizationService;
    private OptimizationAnalyzer optimizationAnalyzer;

    @BeforeEach
    void setUp() {
        usageCalculationService = new UsageCalculationServiceImpl();
        costOptimizationService = new CostOptimizationServiceImpl();
        optimizationAnalyzer = new OptimizationAnalyzerImpl(
                optimizationInsightRepository,
                usageCalculationService,
                costOptimizationService
        );
    }

    @Test
    @DisplayName("Recommendation: < 70% utilization => REDUCE_LICENSES")
    void testDetermineRecommendation_ReduceLicenses() {
        // 64.00% utilization, 18 unused
        OptimizationRecommendation rec = optimizationAnalyzer.determineRecommendation(
                new BigDecimal("64.00"), 18);
        assertThat(rec).isEqualTo(OptimizationRecommendation.REDUCE_LICENSES);
    }

    @Test
    @DisplayName("Recommendation: 70% to 89.99% utilization => REVIEW_USAGE")
    void testDetermineRecommendation_ReviewUsage() {
        // Exactly 70%
        assertThat(optimizationAnalyzer.determineRecommendation(new BigDecimal("70.00"), 30))
                .isEqualTo(OptimizationRecommendation.REVIEW_USAGE);

        // 85%
        assertThat(optimizationAnalyzer.determineRecommendation(new BigDecimal("85.00"), 15))
                .isEqualTo(OptimizationRecommendation.REVIEW_USAGE);

        // 89.99%
        assertThat(optimizationAnalyzer.determineRecommendation(new BigDecimal("89.99"), 10))
                .isEqualTo(OptimizationRecommendation.REVIEW_USAGE);
    }

    @Test
    @DisplayName("Recommendation: >= 90% utilization => NO_ACTION")
    void testDetermineRecommendation_NoAction() {
        // 90.00%
        assertThat(optimizationAnalyzer.determineRecommendation(new BigDecimal("90.00"), 10))
                .isEqualTo(OptimizationRecommendation.NO_ACTION);

        // 95.00%
        assertThat(optimizationAnalyzer.determineRecommendation(new BigDecimal("95.00"), 5))
                .isEqualTo(OptimizationRecommendation.NO_ACTION);

        // 100%
        assertThat(optimizationAnalyzer.determineRecommendation(new BigDecimal("100.00"), 0))
                .isEqualTo(OptimizationRecommendation.NO_ACTION);
    }

    @Test
    @DisplayName("Recommendation: 0 unused capacity always results in NO_ACTION")
    void testDetermineRecommendation_ZeroUnused() {
        assertThat(optimizationAnalyzer.determineRecommendation(new BigDecimal("50.00"), 0))
                .isEqualTo(OptimizationRecommendation.NO_ACTION);
    }

    @Test
    @DisplayName("Analyze usage record: creates new insight with deterministic values")
    void testAnalyzeUsageRecord_CreatesNewInsight() {
        Customer customer = Customer.builder().id(1L).companyName("Acme Corp").build();
        Subscription subscription = Subscription.builder()
                .id(10L)
                .subscriptionNumber("SUB-10")
                .unitPrice(new BigDecimal("2000.00"))
                .customer(customer)
                .build();

        UsageRecord record = UsageRecord.builder()
                .id(100L)
                .customer(customer)
                .subscription(subscription)
                .resourceType("LICENSE")
                .resourceName("Microsoft 365 Licenses")
                .allocatedQuantity(50)
                .usedQuantity(32)
                .unitCost(new BigDecimal("2000.00"))
                .snapshotDate(LocalDate.now())
                .build();

        when(optimizationInsightRepository.findByUsageRecordId(100L)).thenReturn(Optional.empty());
        when(optimizationInsightRepository.save(any(OptimizationInsight.class))).thenAnswer(i -> {
            OptimizationInsight entity = i.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        OptimizationInsight result = optimizationAnalyzer.analyzeUsageRecord(record, null);

        assertThat(result).isNotNull();
        assertThat(result.getAllocatedQuantity()).isEqualTo(50);
        assertThat(result.getUsedQuantity()).isEqualTo(32);
        assertThat(result.getUnusedQuantity()).isEqualTo(18);
        assertThat(result.getUtilizationPercentage()).isEqualByComparingTo(new BigDecimal("64.00"));
        assertThat(result.getUnitCost()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(result.getPotentialMonthlySavings()).isEqualByComparingTo(new BigDecimal("36000.00"));
        assertThat(result.getPotentialAnnualSavings()).isEqualByComparingTo(new BigDecimal("432000.00"));
        assertThat(result.getRecommendation()).isEqualTo(OptimizationRecommendation.REDUCE_LICENSES);
        assertThat(result.getStatus()).isEqualTo(OptimizationStatus.DETECTED);
        verify(optimizationInsightRepository).save(any(OptimizationInsight.class));
    }

    @Test
    @DisplayName("Analyze usage record: duplicate analysis returns existing insight without saving duplicate")
    void testAnalyzeUsageRecord_DuplicateAvoidance() {
        UsageRecord record = UsageRecord.builder().id(200L).build();
        OptimizationInsight existing = OptimizationInsight.builder()
                .id(99L)
                .recommendation(OptimizationRecommendation.REVIEW_USAGE)
                .build();

        when(optimizationInsightRepository.findByUsageRecordId(200L)).thenReturn(Optional.of(existing));

        OptimizationInsight result = optimizationAnalyzer.analyzeUsageRecord(record, null);

        assertThat(result).isSameAs(existing);
        verify(optimizationInsightRepository, never()).save(any(OptimizationInsight.class));
    }
}

