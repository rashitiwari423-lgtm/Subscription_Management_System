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
@Schema(description = "Deterministic analytics result for late payment behavior")
public class LatePaymentResult {

    @Schema(description = "Total number of completed payments made after invoice due date", example = "8")
    private Integer latePaymentCount;

    @Schema(description = "Count of distinct customers with late payment history", example = "3")
    private Integer affectedCustomerCount;

    @Schema(description = "Total monetary volume involved in late payments", example = "62000.00")
    private BigDecimal totalAmountInvolved;

    @Schema(description = "Severity level", example = "WARNING")
    private AiSeverity severity;

    @Schema(description = "Recommended action", example = "REVIEW_LATE_PAYMENTS")
    private AiRecommendedAction recommendedAction;

    @Schema(description = "Customers exhibiting repeated late payments")
    @Builder.Default
    private List<CustomerSummaryDto> affectedCustomers = new ArrayList<>();
}

