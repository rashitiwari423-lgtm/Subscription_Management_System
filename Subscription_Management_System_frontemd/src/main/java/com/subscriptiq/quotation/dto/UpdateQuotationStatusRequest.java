package com.subscriptiq.quotation.dto;

import com.subscriptiq.quotation.enums.QuotationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update quotation lifecycle status")
public class UpdateQuotationStatusRequest {

    @NotNull(message = "Status is required")
    @Schema(description = "New quotation status (SENT, ACCEPTED, REJECTED)", example = "SENT")
    private QuotationStatus status;
}

