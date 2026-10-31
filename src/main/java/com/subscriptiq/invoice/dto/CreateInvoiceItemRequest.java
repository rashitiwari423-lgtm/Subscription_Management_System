package com.subscriptiq.invoice.dto;

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
public class CreateInvoiceItemRequest {

    private Long planId;

    private String description;

    @NotNull(message = "Item quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Item unit price is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Unit price cannot be negative")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.00", inclusive = true, message = "Discount amount cannot be negative")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0.00", inclusive = true, message = "Tax amount cannot be negative")
    private BigDecimal taxAmount;
}

