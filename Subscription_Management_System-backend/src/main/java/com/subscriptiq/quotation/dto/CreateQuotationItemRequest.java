package com.subscriptiq.quotation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request item for quotation line")
public class CreateQuotationItemRequest {

    @NotNull(message = "Plan ID is required")
    @Schema(description = "ID of the target subscription plan", example = "1")
    private Long planId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Quantity of subscriptions", example = "5")
    private Integer quantity;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discount amount cannot be negative")
    @Schema(description = "Line item discount amount", example = "50.00")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Tax amount cannot be negative")
    @Schema(description = "Line item tax amount", example = "18.00")
    private BigDecimal taxAmount;
}

