package com.subscriptiq.ai.dto;

import com.subscriptiq.ai.enums.AiRecommendedAction;
import com.subscriptiq.ai.enums.AiSeverity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Deterministic cost optimization summary reusing Phase 7 insight calculations")
public class OptimizationSummaryResult {

    @Schema(description = "Count of active optimization insights", example = "2")
    private Integer insightCount;

    @Schema(description = "Total unused units identified across recommendations", example = "18")
    private Integer unusedCapacityTotal;

    @Schema(description = "Deterministic potential monthly savings from persisted data", example = "36000.00")
    private BigDecimal potentialMonthlySavings;

    @Schema(description = "Deterministic potential annual savings (monthly * 12)", example = "432000.00")
    private BigDecimal potentialAnnualSavings;

    @Schema(description = "Count of customers with savings opportunities", example = "1")
    private Integer affectedCustomerCount;

    @Schema(description = "Severity level", example = "WARNING")
    private AiSeverity severity;

    @Schema(description = "Recommended action", example = "REVIEW_OPTIMIZATION")
    private AiRecommendedAction recommendedAction;

    @Schema(description = "Details of identified savings per customer/resource")
    @Builder.Default
    private List<CustomerSummaryDto> recommendations = new ArrayList<>();
}

