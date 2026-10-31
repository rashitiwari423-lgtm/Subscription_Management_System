package com.subscriptiq.ai.dto;

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
@Schema(description = "Deterministic summary of plan performance, subscription distribution, and revenue contribution")
public class PlanPerformanceResult {

    @Schema(description = "Total distinct active subscription plans", example = "4")
    private Integer totalPlans;

    @Schema(description = "Breakdown per plan")
    @Builder.Default
    private List<PlanStatDto> planStats = new ArrayList<>();

    @Schema(description = "Explicit compliance note regarding predictive churn", example = "Predictive churn is reserved for future analytics phases.")
    @Builder.Default
    private String note = "Predictive churn is not yet available in Phase 8.";

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Statistics for an individual plan")
    public static class PlanStatDto {
        private Long planId;
        private String planName;
        private String billingCycle;
        private BigDecimal price;
        private Integer activeSubscriptions;
        private Integer pausedOrClosedSubscriptions;
        private BigDecimal monthlyRevenue;
    }
}

