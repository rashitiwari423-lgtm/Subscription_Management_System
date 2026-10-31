package com.subscriptiq.tax.dto;

import com.subscriptiq.tax.enums.TaxStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaxStatusRequest {

    @NotNull(message = "Tax status is required")
    private TaxStatus status;
}

