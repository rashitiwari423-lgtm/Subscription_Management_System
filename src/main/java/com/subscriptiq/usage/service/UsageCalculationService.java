package com.subscriptiq.usage.service;

import java.math.BigDecimal;

public interface UsageCalculationService {

    Integer calculateUnusedQuantity(Integer allocatedQuantity, Integer usedQuantity);

    BigDecimal calculateUtilizationPercentage(Integer allocatedQuantity, Integer usedQuantity);
}

