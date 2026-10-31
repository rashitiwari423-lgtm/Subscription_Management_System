package com.subscriptiq.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueForecastResponse {

    private String methodology;
    private String confidence;

    @JsonProperty("isSufficientData")
    private boolean isSufficientData;
    private String message;
    private int historicalPeriodMonths;
    private BigDecimal averageMonthlyRevenue;
    private List<HistoricalMonthlyRevenueDto> historicalRevenue;
    private List<ForecastMonthDto> forecast;
    private String forecastPeriod;
}
