package com.subscriptiq.optimization.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CostOptimizationServiceTest {

    private CostOptimizationService costOptimizationService;

    @BeforeEach
    void setUp() {
        costOptimizationService = new CostOptimizationServiceImpl();
    }

    @Test
    @DisplayName("Calculate monthly savings: 18 unused * 2000 unitCost = 36000.00")
    void testCalculateMonthlySavings_Success() {
        BigDecimal savings = costOptimizationService.calculateMonthlySavings(18, new BigDecimal("2000.00"));
        assertThat(savings).isEqualByComparingTo(new BigDecimal("36000.00"));
    }

    @Test
    @DisplayName("Calculate annual savings: 36000 monthly * 12 = 432000.00")
    void testCalculateAnnualSavings_Success() {
        BigDecimal annual = costOptimizationService.calculateAnnualSavings(new BigDecimal("36000.00"));
        assertThat(annual).isEqualByComparingTo(new BigDecimal("432000.00"));
    }

    @Test
    @DisplayName("No savings when fully utilized (unused = 0)")
    void testCalculateMonthlySavings_ZeroUnused() {
        BigDecimal savings = costOptimizationService.calculateMonthlySavings(0, new BigDecimal("2000.00"));
        assertThat(savings).isEqualByComparingTo(new BigDecimal("0.00"));

        BigDecimal annual = costOptimizationService.calculateAnnualSavings(savings);
        assertThat(annual).isEqualByComparingTo(new BigDecimal("0.00"));
    }

    @Test
    @DisplayName("Calculate current monthly cost: 50 allocated * 2000 = 100000.00")
    void testCalculateCurrentMonthlyCost() {
        BigDecimal currentCost = costOptimizationService.calculateCurrentMonthlyCost(50, new BigDecimal("2000.00"));
        assertThat(currentCost).isEqualByComparingTo(new BigDecimal("100000.00"));
    }

    @Test
    @DisplayName("Handle null or negative values safely")
    void testCalculateSavings_NullSafe() {
        assertThat(costOptimizationService.calculateMonthlySavings(null, new BigDecimal("2000.00")))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(costOptimizationService.calculateMonthlySavings(10, null))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(costOptimizationService.calculateAnnualSavings(null))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(costOptimizationService.calculateCurrentMonthlyCost(null, null))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }
}

