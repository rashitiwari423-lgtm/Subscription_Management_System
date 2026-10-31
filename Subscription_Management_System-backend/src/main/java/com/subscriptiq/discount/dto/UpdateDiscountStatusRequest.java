package com.subscriptiq.discount.dto;

import com.subscriptiq.discount.enums.DiscountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDiscountStatusRequest {

    @NotNull(message = "Discount status is required")
    private DiscountStatus status;
}

