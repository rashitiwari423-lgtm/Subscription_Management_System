package com.subscriptiq.plan.dto;

import com.subscriptiq.plan.enums.BillingCycle;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
public class UpdatePlanRequest {

    @NotBlank(message = "Plan name is required")
    @Size(min = 2, max = 150, message = "Plan name must be between 2 and 150 characters")
    private String name;

    private String description;

    @NotNull(message = "Billing cycle is required (MONTHLY, QUARTERLY, YEARLY)")
    private BillingCycle billingCycle;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    @Size(min = 3, max = 10, message = "Currency code must be valid")
    private String currency;

    @Min(value = 0, message = "Trial days cannot be negative")
    private Integer trialDays;
}

