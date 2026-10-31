package com.subscriptiq.quotation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
@Schema(description = "Request to update a draft quotation")
public class UpdateQuotationRequest {

    @Schema(description = "Updated valid until date", example = "2026-12-15")
    private LocalDate validUntil;

    @Schema(description = "Updated notes or terms", example = "Updated discount terms")
    private String notes;

    @Valid
    @Schema(description = "Updated line items (replaces existing line items if provided)")
    private List<CreateQuotationItemRequest> items;
}

