package com.subscriptiq.product.dto;

import com.subscriptiq.product.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductStatusRequest {

    @NotNull(message = "Status is required (ACTIVE or INACTIVE)")
    private ProductStatus status;
}

