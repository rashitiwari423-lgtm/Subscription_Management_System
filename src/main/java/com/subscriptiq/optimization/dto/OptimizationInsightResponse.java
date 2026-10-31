package com.subscriptiq.optimization.dto;

import com.subscriptiq.optimization.enums.OptimizationRecommendation;
import com.subscriptiq.optimization.enums.OptimizationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response representation of a cost optimization insight and recommendation")
public class OptimizationInsightResponse {

    @Schema(description = "Insight unique ID", example = "1")
    private Long id;

    @Schema(description = "Customer ID", example = "1")
    private Long customerId;

    @Schema(description = "Customer company name", example = "Acme Corp")
    private String customerCompanyName;

    @Schema(description = "Subscription ID", example = "1")
    private Long subscriptionId;

    @Schema(description = "Subscription number", example = "SUB-20261031-1001")
    private String subscriptionNumber;

    @Schema(description = "Linked usage record ID", example = "1")
    private Long usageRecordId;

    @Schema(description = "Resource name", example = "Microsoft 365 Licenses")
    private String resourceName;

    @Schema(description = "Allocated quantity", example = "50")
    private Integer allocatedQuantity;

    @Schema(description = "Used quantity", example = "32")
    private Integer usedQuantity;

    @Schema(description = "Unused quantity", example = "18")
    private Integer unusedQuantity;

    @Schema(description = "Utilization percentage", example = "64.00")
    private BigDecimal utilizationPercentage;

    @Schema(description = "Unit cost", example = "2000.00")
    private BigDecimal unitCost;

    @Schema(description = "Current monthly cost based on total allocation", example = "100000.00")
    private BigDecimal currentMonthlyCost;

    @Schema(description = "Potential monthly savings from unused capacity", example = "36000.00")
    private BigDecimal potentialMonthlySavings;

    @Schema(description = "Potential annual savings from unused capacity (monthly * 12)", example = "432000.00")
    private BigDecimal potentialAnnualSavings;

    @Schema(description = "Optimization recommendation", example = "REDUCE_LICENSES")
    private OptimizationRecommendation recommendation;

    @Schema(description = "Actionable proposed recommendation summary", example = "Reduce 18 unassigned Microsoft 365 Licenses to save 36000.00/month")
    private String proposedAction;

    @Schema(description = "Approval/lifecycle status", example = "PREVIEWED")
    private OptimizationStatus status;

    @Schema(description = "Optional review notes", example = "Approved for next renewal cycle")
    private String notes;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}

