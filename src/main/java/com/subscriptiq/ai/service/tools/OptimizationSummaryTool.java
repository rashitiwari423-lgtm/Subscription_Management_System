package com.subscriptiq.ai.service.tools;

import com.subscriptiq.ai.dto.CustomerSummaryDto;
import com.subscriptiq.ai.dto.OptimizationSummaryResult;
import com.subscriptiq.ai.enums.AiRecommendedAction;
import com.subscriptiq.ai.enums.AiSeverity;
import com.subscriptiq.optimization.entity.OptimizationInsight;
import com.subscriptiq.optimization.enums.OptimizationStatus;
import com.subscriptiq.optimization.repository.OptimizationInsightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class OptimizationSummaryTool {

    private final OptimizationInsightRepository optimizationInsightRepository;

    public OptimizationSummaryResult analyze() {
        // Query insights that have not been executed or rejected
        List<OptimizationInsight> allInsights = optimizationInsightRepository.findAll();
        List<OptimizationInsight> activeInsights = allInsights.stream()
                .filter(i -> i.getStatus() != OptimizationStatus.REJECTED && i.getStatus() != OptimizationStatus.EXECUTED)
                .toList();

        int unusedCapacityTotal = 0;
        BigDecimal totalMonthlySavings = BigDecimal.ZERO;
        BigDecimal totalAnnualSavings = BigDecimal.ZERO;
        Set<Long> affectedCustomerIds = new HashSet<>();
        List<CustomerSummaryDto> recommendations = new ArrayList<>();

        for (OptimizationInsight insight : activeInsights) {
            int unused = insight.getUnusedQuantity() != null ? insight.getUnusedQuantity() : 0;
            unusedCapacityTotal += unused;

            BigDecimal monthly = insight.getPotentialMonthlySavings() != null ? insight.getPotentialMonthlySavings() : BigDecimal.ZERO;
            BigDecimal annual = insight.getPotentialAnnualSavings() != null ? insight.getPotentialAnnualSavings() : BigDecimal.ZERO;

            totalMonthlySavings = totalMonthlySavings.add(monthly);
            totalAnnualSavings = totalAnnualSavings.add(annual);

            if (insight.getCustomer() != null) {
                affectedCustomerIds.add(insight.getCustomer().getId());
                recommendations.add(CustomerSummaryDto.builder()
                        .customerId(insight.getCustomer().getId())
                        .companyName(insight.getCustomer().getCompanyName())
                        .email(insight.getCustomer().getEmail())
                        .amount(monthly)
                        .detail(String.format("%s: %s (utilization %s%%, %d idle units)",
                                insight.getResourceName(), insight.getRecommendation(), insight.getUtilizationPercentage(), unused))
                        .build());
            }
        }

        AiSeverity severity = totalMonthlySavings.compareTo(BigDecimal.ZERO) > 0
                ? (totalMonthlySavings.compareTo(new BigDecimal("50000.00")) >= 0 ? AiSeverity.CRITICAL : AiSeverity.WARNING)
                : AiSeverity.INFO;

        return OptimizationSummaryResult.builder()
                .insightCount(activeInsights.size())
                .unusedCapacityTotal(unusedCapacityTotal)
                .potentialMonthlySavings(totalMonthlySavings.setScale(2, RoundingMode.HALF_UP))
                .potentialAnnualSavings(totalAnnualSavings.setScale(2, RoundingMode.HALF_UP))
                .affectedCustomerCount(affectedCustomerIds.size())
                .severity(severity)
                .recommendedAction(AiRecommendedAction.REVIEW_OPTIMIZATION)
                .recommendations(recommendations)
                .build();
    }
}

