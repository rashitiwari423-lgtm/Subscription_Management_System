package com.subscriptiq.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Deterministic summary of customer subscriptions across lifecycle statuses")
public class SubscriptionSummaryResult {

    @Schema(description = "Total subscriptions across all statuses", example = "50")
    private Integer totalSubscriptions;

    @Schema(description = "Count of ACTIVE subscriptions", example = "42")
    private Integer activeSubscriptions;

    @Schema(description = "Count of PAUSED subscriptions", example = "5")
    private Integer pausedSubscriptions;

    @Schema(description = "Count of EXPIRED or CLOSED subscriptions", example = "3")
    private Integer closedOrExpiredSubscriptions;

    @Schema(description = "Estimated monthly recurring revenue (MRR) from active subscriptions", example = "125000.00")
    private BigDecimal monthlyRecurringRevenue;
}

