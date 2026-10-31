package com.subscriptiq.usage.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class UsageCalculationServiceImpl implements UsageCalculationService {

    @Override
    public Integer calculateUnusedQuantity(Integer allocatedQuantity, Integer usedQuantity) {
        int allocated = allocatedQuantity != null ? allocatedQuantity : 0;
        int used = usedQuantity != null ? usedQuantity : 0;
        return Math.max(0, allocated - used);
    }

    @Override
    public BigDecimal calculateUtilizationPercentage(Integer allocatedQuantity, Integer usedQuantity) {
        if (allocatedQuantity == null || allocatedQuantity <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        int used = usedQuantity != null ? Math.max(0, usedQuantity) : 0;
        BigDecimal usedDecimal = BigDecimal.valueOf(used);
        BigDecimal allocatedDecimal = BigDecimal.valueOf(allocatedQuantity);

        return usedDecimal.divide(allocatedDecimal, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}

