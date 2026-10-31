package com.subscriptiq.optimization.analyzer;

import com.subscriptiq.optimization.entity.OptimizationInsight;
import com.subscriptiq.optimization.enums.OptimizationRecommendation;
import com.subscriptiq.usage.entity.UsageRecord;

import java.math.BigDecimal;

public interface OptimizationAnalyzer {

    OptimizationRecommendation determineRecommendation(BigDecimal utilizationPercentage, Integer unusedQuantity);

    String generateProposedAction(
            OptimizationRecommendation recommendation,
            String resourceName,
            Integer unusedQuantity,
            BigDecimal monthlySavings,
            BigDecimal utilizationPercentage);

    OptimizationInsight analyzeUsageRecord(UsageRecord usageRecord, BigDecimal unitCostOverride);
}

