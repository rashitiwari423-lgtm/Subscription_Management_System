package com.subscriptiq.optimization.analyzer;

import com.subscriptiq.optimization.entity.OptimizationInsight;
import com.subscriptiq.optimization.enums.OptimizationRecommendation;
import com.subscriptiq.optimization.enums.OptimizationStatus;
import com.subscriptiq.optimization.repository.OptimizationInsightRepository;
import com.subscriptiq.optimization.service.CostOptimizationService;
import com.subscriptiq.usage.entity.UsageRecord;
import com.subscriptiq.usage.service.UsageCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OptimizationAnalyzerImpl implements OptimizationAnalyzer {

    private final OptimizationInsightRepository optimizationInsightRepository;
    private final UsageCalculationService usageCalculationService;
    private final CostOptimizationService costOptimizationService;

    @Override
    public OptimizationRecommendation determineRecommendation(BigDecimal utilizationPercentage, Integer unusedQuantity) {
        if (unusedQuantity == null || unusedQuantity <= 0) {
            return OptimizationRecommendation.NO_ACTION;
        }

        BigDecimal utilization = utilizationPercentage != null ? utilizationPercentage : BigDecimal.ZERO;

        if (utilization.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return OptimizationRecommendation.NO_ACTION;
        } else if (utilization.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return OptimizationRecommendation.REVIEW_USAGE;
        } else {
            return OptimizationRecommendation.REDUCE_LICENSES;
        }
    }

    @Override
    public String generateProposedAction(
            OptimizationRecommendation recommendation,
            String resourceName,
            Integer unusedQuantity,
            BigDecimal monthlySavings,
            BigDecimal utilizationPercentage) {

        int unused = unusedQuantity != null ? unusedQuantity : 0;
        BigDecimal savings = monthlySavings != null ? monthlySavings : BigDecimal.ZERO;
        BigDecimal util = utilizationPercentage != null ? utilizationPercentage : BigDecimal.ZERO;

        return switch (recommendation) {
            case REDUCE_LICENSES -> String.format(
                    "Reduce %d unassigned %s to save %s/month (current utilization: %s%%)",
                    unused, resourceName, savings, util);
            case REVIEW_USAGE -> String.format(
                    "Review usage pattern for %s; moderate utilization of %s%% with %d idle capacity (potential monthly savings: %s)",
                    resourceName, util, unused, savings);
            case NO_ACTION -> String.format(
                    "Healthy resource allocation for %s; high utilization of %s%% (%d idle capacity)",
                    resourceName, util, unused);
        };
    }

    @Override
    public OptimizationInsight analyzeUsageRecord(UsageRecord usageRecord, BigDecimal unitCostOverride) {
        // Prevent duplicate insights for the same usage record snapshot
        Optional<OptimizationInsight> existing = optimizationInsightRepository.findByUsageRecordId(usageRecord.getId());
        if (existing.isPresent()) {
            log.info("Optimization insight already exists for usage record [{}], returning existing insight [{}]",
                    usageRecord.getId(), existing.get().getId());
            return existing.get();
        }

        Integer unusedQuantity = usageCalculationService.calculateUnusedQuantity(
                usageRecord.getAllocatedQuantity(), usageRecord.getUsedQuantity());
        BigDecimal utilizationPercentage = usageCalculationService.calculateUtilizationPercentage(
                usageRecord.getAllocatedQuantity(), usageRecord.getUsedQuantity());

        BigDecimal unitCost = unitCostOverride;
        if (unitCost == null) {
            unitCost = usageRecord.getUnitCost();
            if (unitCost == null && usageRecord.getSubscription() != null) {
                unitCost = usageRecord.getSubscription().getUnitPrice();
                if (unitCost == null && usageRecord.getSubscription().getPlan() != null) {
                    unitCost = usageRecord.getSubscription().getPlan().getPrice();
                }
            }
            if (unitCost == null) {
                unitCost = BigDecimal.ZERO;
            }
        }

        OptimizationRecommendation recommendation = determineRecommendation(utilizationPercentage, unusedQuantity);
        BigDecimal monthlySavings = costOptimizationService.calculateMonthlySavings(unusedQuantity, unitCost);
        BigDecimal annualSavings = costOptimizationService.calculateAnnualSavings(monthlySavings);
        String proposedAction = generateProposedAction(
                recommendation, usageRecord.getResourceName(), unusedQuantity, monthlySavings, utilizationPercentage);

        OptimizationInsight insight = OptimizationInsight.builder()
                .customer(usageRecord.getCustomer())
                .subscription(usageRecord.getSubscription())
                .usageRecord(usageRecord)
                .resourceName(usageRecord.getResourceName())
                .allocatedQuantity(usageRecord.getAllocatedQuantity())
                .usedQuantity(usageRecord.getUsedQuantity())
                .unusedQuantity(unusedQuantity)
                .utilizationPercentage(utilizationPercentage)
                .unitCost(unitCost)
                .potentialMonthlySavings(monthlySavings)
                .potentialAnnualSavings(annualSavings)
                .status(OptimizationStatus.DETECTED)
                .recommendation(recommendation)
                .notes(proposedAction)
                .build();

        OptimizationInsight saved = optimizationInsightRepository.save(insight);
        log.info("Created optimization insight [{}] for customer [{}] with recommendation [{}] and monthly savings [{}]",
                saved.getId(), saved.getCustomer().getId(), saved.getRecommendation(), saved.getPotentialMonthlySavings());

        return saved;
    }
}

