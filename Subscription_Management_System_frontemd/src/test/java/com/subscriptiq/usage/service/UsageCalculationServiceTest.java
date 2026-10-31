package com.subscriptiq.usage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

class UsageCalculationServiceTest {

    private UsageCalculationService usageCalculationService;

    @BeforeEach
    void setUp() {
        usageCalculationService = new UsageCalculationServiceImpl();
    }

    @Test
    @DisplayName("Calculate unused quantity when used is less than allocated")
    void testCalculateUnusedQuantity_Partial() {
        Integer unused = usageCalculationService.calculateUnusedQuantity(50, 32);
        assertThat(unused).isEqualTo(18);
    }

    @Test
    @DisplayName("Calculate unused quantity when fully utilized")
    void testCalculateUnusedQuantity_FullyUtilized() {
        Integer unused = usageCalculationService.calculateUnusedQuantity(50, 50);
        assertThat(unused).isEqualTo(0);
    }

    @Test
    @DisplayName("Calculate unused quantity with null or negative inputs")
    void testCalculateUnusedQuantity_NullOrZero() {
        assertThat(usageCalculationService.calculateUnusedQuantity(null, 10)).isEqualTo(0);
        assertThat(usageCalculationService.calculateUnusedQuantity(50, null)).isEqualTo(50);
        assertThat(usageCalculationService.calculateUnusedQuantity(20, 25)).isEqualTo(0);
    }

    @Test
    @DisplayName("Calculate utilization percentage for partial utilization (32 / 50 = 64.00%)")
    void testCalculateUtilizationPercentage_Partial() {
        BigDecimal utilization = usageCalculationService.calculateUtilizationPercentage(50, 32);
        assertThat(utilization).isEqualByComparingTo(new BigDecimal("64.00"));
    }

    @Test
    @DisplayName("Calculate utilization percentage for 100% utilization")
    void testCalculateUtilizationPercentage_Full() {
        BigDecimal utilization = usageCalculationService.calculateUtilizationPercentage(100, 100);
        assertThat(utilization).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Calculate utilization percentage safely when allocated is zero (prevent division by zero)")
    void testCalculateUtilizationPercentage_ZeroAllocated() {
        BigDecimal utilization = usageCalculationService.calculateUtilizationPercentage(0, 0);
        assertThat(utilization).isEqualByComparingTo(new BigDecimal("0.00"));

        BigDecimal utilizationNull = usageCalculationService.calculateUtilizationPercentage(null, 5);
        assertThat(utilizationNull).isEqualByComparingTo(new BigDecimal("0.00"));
    }

    @Test
    @DisplayName("Calculate utilization percentage with rounding")
    void testCalculateUtilizationPercentage_Rounding() {
        // 1 / 3 = 33.33%
        BigDecimal utilization = usageCalculationService.calculateUtilizationPercentage(3, 1);
        assertThat(utilization).isEqualByComparingTo(new BigDecimal("33.33"));
    }
}

