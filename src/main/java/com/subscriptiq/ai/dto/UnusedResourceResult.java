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
@Schema(description = "Deterministic summary of underutilized and idle resources")
public class UnusedResourceResult {

    @Schema(description = "Count of usage records with idle capacity", example = "3")
    private Integer unusedRecordCount;

    @Schema(description = "Total allocated capacity across resources", example = "150")
    private Integer totalAllocated;

    @Schema(description = "Total actively consumed capacity", example = "105")
    private Integer totalUsed;

    @Schema(description = "Total idle/unassigned capacity", example = "45")
    private Integer totalUnused;

    @Schema(description = "Count of customers with idle resource capacity", example = "2")
    private Integer affectedCustomerCount;

    @Schema(description = "Severity level based on idle overhead", example = "WARNING")
    private AiSeverity severity;

    @Schema(description = "Recommended action", example = "REVIEW_UNUSED_RESOURCES")
    private AiRecommendedAction recommendedAction;

    @Schema(description = "Breakdown of underutilized resources")
    @Builder.Default
    private List<CustomerSummaryDto> resources = new ArrayList<>();
}

