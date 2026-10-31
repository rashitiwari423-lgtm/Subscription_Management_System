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
@Schema(description = "Deterministic financial summary of invoiced, collected, and outstanding revenue")
public class RevenueSummaryResult {

    @Schema(description = "Total invoiced amount across all invoices", example = "250000.00")
    private BigDecimal totalInvoiced;

    @Schema(description = "Total revenue collected via completed payments", example = "180000.00")
    private BigDecimal totalCollected;

    @Schema(description = "Total outstanding uncollected balance", example = "70000.00")
    private BigDecimal totalOutstanding;

    @Schema(description = "Collection efficiency rate (collected / invoiced * 100)", example = "72.00")
    private BigDecimal collectionRatePercentage;

    @Schema(description = "Total invoice count", example = "25")
    private Integer totalInvoices;

    @Schema(description = "Paid invoice count", example = "18")
    private Integer paidInvoices;

    @Schema(description = "Overdue invoice count", example = "4")
    private Integer overdueInvoices;
}

