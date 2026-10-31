package com.subscriptiq.quotation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new quotation")
public class CreateQuotationRequest {

    @NotNull(message = "Customer ID is required")
    @Schema(description = "Customer ID who will receive the quotation", example = "1")
    private Long customerId;

    @Schema(description = "Date of quotation issuance (defaults to today)", example = "2026-10-31")
    private LocalDate quotationDate;

    @NotNull(message = "Valid until date is required")
    @Schema(description = "Expiration date of quotation", example = "2026-11-30")
    private LocalDate validUntil;

    @Schema(description = "Additional notes or terms", example = "Standard enterprise terms apply")
    private String notes;

    @NotEmpty(message = "Quotation must have at least one line item")
    @Valid
    @Schema(description = "List of quotation items")
    private List<CreateQuotationItemRequest> items;
}

