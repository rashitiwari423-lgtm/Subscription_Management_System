package com.subscriptiq.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilizationSummaryDto {

    private Double averageUtilizationPercentage;
    private int underutilizedCount;
    private int optimalCount;
}
