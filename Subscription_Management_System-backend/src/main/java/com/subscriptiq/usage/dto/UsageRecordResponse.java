package com.subscriptiq.usage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response representation of a resource usage record")
public class UsageRecordResponse {

    @Schema(description = "Unique ID of the usage record", example = "1")
    private Long id;

    @Schema(description = "Customer ID", example = "1")
    private Long customerId;

    @Schema(description = "Customer company name", example = "Acme Corp")
    private String customerCompanyName;

    @Schema(description = "Subscription ID", example = "1")
    private Long subscriptionId;

    @Schema(description = "Subscription number", example = "SUB-20261031-1001")
    private String subscriptionNumber;

    @Schema(description = "Resource type", example = "LICENSE")
    private String resourceType;

    @Schema(description = "Resource name", example = "Microsoft 365 Licenses")
    private String resourceName;

    @Schema(description = "Allocated quantity", example = "50")
    private Integer allocatedQuantity;

    @Schema(description = "Used quantity", example = "32")
    private Integer usedQuantity;

    @Schema(description = "Calculated unused quantity", example = "18")
    private Integer unusedQuantity;

    @Schema(description = "Calculated utilization percentage", example = "64.00")
    private BigDecimal utilizationPercentage;

    @Schema(description = "Unit cost", example = "2000.00")
    private BigDecimal unitCost;

    @Schema(description = "Snapshot date", example = "2026-10-31")
    private LocalDate snapshotDate;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record last update timestamp")
    private LocalDateTime updatedAt;
}

