package com.subscriptiq.tax.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaxRequest {

    @NotBlank(message = "Tax name is required")
    @Size(max = 100, message = "Tax name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Tax percentage is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Percentage cannot be less than 0")
    @DecimalMax(value = "100.00", inclusive = true, message = "Percentage cannot be greater than 100")
    @Digits(integer = 3, fraction = 2, message = "Percentage must have up to 3 integer digits and 2 decimal places")
    private BigDecimal percentage;
}

