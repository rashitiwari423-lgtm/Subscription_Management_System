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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Deterministic analytics result for expiring subscriptions")
public class ExpiringSubscriptionResult {

    @Schema(description = "Count of subscriptions approaching expiration", example = "4")
    private Integer count;

    @Schema(description = "Earliest expiry date among expiring subscriptions", example = "2026-11-15")
    private LocalDate earliestExpiryDate;

    @Schema(description = "Potential recurring revenue at risk", example = "86000.00")
    private BigDecimal potentialRevenue;

    @Schema(description = "Severity level", example = "CRITICAL")
    private AiSeverity severity;

    @Schema(description = "Recommended action", example = "SEND_RENEWAL_CAMPAIGN")
    private AiRecommendedAction recommendedAction;

    @Schema(description = "Customers with expiring subscriptions")
    @Builder.Default
    private List<CustomerSummaryDto> customers = new ArrayList<>();
}

