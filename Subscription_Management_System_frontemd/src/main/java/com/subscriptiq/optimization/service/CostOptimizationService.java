package com.subscriptiq.optimization.service;

import java.math.BigDecimal;

public interface CostOptimizationService {

    BigDecimal calculateMonthlySavings(Integer unusedQuantity, BigDecimal unitCost);

    BigDecimal calculateAnnualSavings(BigDecimal potentialMonthlySavings);

    BigDecimal calculateCurrentMonthlyCost(Integer allocatedQuantity, BigDecimal unitCost);
}

