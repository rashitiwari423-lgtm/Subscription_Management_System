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
@Schema(description = "Deterministic analytics result for overdue payments")
public class OverduePaymentResult {

    @Schema(description = "Count of overdue invoices", example = "5")
    private Integer count;

    @Schema(description = "Total overdue balance across all overdue invoices", example = "45000.00")
    private BigDecimal totalAmount;

    @Schema(description = "Oldest overdue due date", example = "2026-09-15")
    private LocalDate oldestDueDate;

    @Schema(description = "Oldest overdue invoice number", example = "INV-20260915-001")
    private String oldestInvoiceNumber;

    @Schema(description = "Severity level based on overdue exposure", example = "CRITICAL")
    private AiSeverity severity;

    @Schema(description = "Recommended operational action", example = "FOLLOW_UP_OVERDUE_CUSTOMERS")
    private AiRecommendedAction recommendedAction;

    @Schema(description = "Top customers affected by overdue invoices")
    @Builder.Default
    private List<CustomerSummaryDto> topAffectedCustomers = new ArrayList<>();
}

