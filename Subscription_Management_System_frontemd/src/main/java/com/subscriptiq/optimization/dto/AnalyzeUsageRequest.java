package com.subscriptiq.optimization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
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
@Schema(description = "Request payload to trigger deterministic cost optimization analysis")
public class AnalyzeUsageRequest {

    @Schema(description = "Optional specific usage record ID to analyze. If omitted, analyzes unanalyzed records matching customer/subscription or system-wide.", example = "1")
    private Long usageRecordId;

    @Schema(description = "Optional customer ID to scope the analysis", example = "1")
    private Long customerId;

    @Schema(description = "Optional subscription ID to scope the analysis", example = "1")
    private Long subscriptionId;

    @DecimalMin(value = "0.00", message = "Unit cost override cannot be negative")
    @Schema(description = "Optional unit cost override for cost saving calculations", example = "2000.00")
    private BigDecimal unitCostOverride;
}

