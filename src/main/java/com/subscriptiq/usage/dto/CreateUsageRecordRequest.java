package com.subscriptiq.usage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to record resource usage snapshot")
public class CreateUsageRecordRequest {

    @NotNull(message = "Customer ID is required")
    @Schema(description = "ID of the customer", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long customerId;

    @NotNull(message = "Subscription ID is required")
    @Schema(description = "ID of the associated subscription", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long subscriptionId;

    @NotBlank(message = "Resource type is required")
    @Schema(description = "Type/category of the resource (e.g. LICENSE, SEATS, STORAGE)", example = "LICENSE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String resourceType;

    @NotBlank(message = "Resource name is required")
    @Schema(description = "Descriptive name of the resource", example = "Microsoft 365 Licenses", requiredMode = Schema.RequiredMode.REQUIRED)
    private String resourceName;

    @NotNull(message = "Allocated quantity is required")
    @Min(value = 0, message = "Allocated quantity cannot be negative")
    @Schema(description = "Total allocated capacity/licenses", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer allocatedQuantity;

    @NotNull(message = "Used quantity is required")
    @Min(value = 0, message = "Used quantity cannot be negative")
    @Schema(description = "Actually consumed/assigned quantity", example = "32", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer usedQuantity;

    @DecimalMin(value = "0.00", message = "Unit cost cannot be negative")
    @Schema(description = "Optional cost per unit. Defaults to subscription unit price if omitted.", example = "2000.00")
    private BigDecimal unitCost;

    @NotNull(message = "Snapshot date is required")
    @Schema(description = "Date of the usage snapshot", example = "2026-10-31", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate snapshotDate;
}

