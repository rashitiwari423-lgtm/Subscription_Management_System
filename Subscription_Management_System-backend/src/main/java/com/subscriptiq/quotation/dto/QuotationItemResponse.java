package com.subscriptiq.quotation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Quotation line item details")
public class QuotationItemResponse {

    @Schema(description = "Line item ID", example = "1")
    private Long id;

    @Schema(description = "Plan ID", example = "10")
    private Long planId;

    @Schema(description = "Plan name", example = "Pro Annual")
    private String planName;

    @Schema(description = "Quantity", example = "2")
    private Integer quantity;

    @Schema(description = "Unit price snapshot", example = "999.00")
    private BigDecimal unitPrice;

    @Schema(description = "Discount amount", example = "50.00")
    private BigDecimal discountAmount;

    @Schema(description = "Tax amount", example = "18.00")
    private BigDecimal taxAmount;

    @Schema(description = "Line total amount", example = "1966.00")
    private BigDecimal totalAmount;
}

