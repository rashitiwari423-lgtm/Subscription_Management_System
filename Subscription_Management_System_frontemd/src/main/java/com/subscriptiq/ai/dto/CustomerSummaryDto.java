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
@Schema(description = "Summary of a customer affected by an analytics insight")
public class CustomerSummaryDto {

    @Schema(description = "Customer ID", example = "1")
    private Long customerId;

    @Schema(description = "Customer company name", example = "Acme Corp")
    private String companyName;

    @Schema(description = "Customer contact email", example = "contact@acme.com")
    private String email;

    @Schema(description = "Monetary amount involved (if applicable)", example = "15000.00")
    private BigDecimal amount;

    @Schema(description = "Contextual detail (e.g. invoice number, days overdue, resource name)", example = "Invoice INV-20261031-001 (15 days overdue)")
    private String detail;
}

