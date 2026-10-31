package com.subscriptiq.product.dto;

import com.subscriptiq.product.enums.VariantStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVariantStatusRequest {

    @NotNull(message = "Status is required (ACTIVE or INACTIVE)")
    private VariantStatus status;
}

