package com.subscriptiq.optimization.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CostOptimizationServiceImpl implements CostOptimizationService {

    @Override
    public BigDecimal calculateMonthlySavings(Integer unusedQuantity, BigDecimal unitCost) {
        if (unusedQuantity == null || unusedQuantity <= 0 || unitCost == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(unusedQuantity)
                .multiply(unitCost)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateAnnualSavings(BigDecimal potentialMonthlySavings) {
        if (potentialMonthlySavings == null || potentialMonthlySavings.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return potentialMonthlySavings.multiply(BigDecimal.valueOf(12))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateCurrentMonthlyCost(Integer allocatedQuantity, BigDecimal unitCost) {
        if (allocatedQuantity == null || allocatedQuantity <= 0 || unitCost == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(allocatedQuantity)
                .multiply(unitCost)
                .setScale(2, RoundingMode.HALF_UP);
    }
}

