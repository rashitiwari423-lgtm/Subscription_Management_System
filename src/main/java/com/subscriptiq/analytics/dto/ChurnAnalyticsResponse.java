package com.subscriptiq.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChurnAnalyticsResponse {

    private Double churnRate;
    private long closedSubscriptions;
    private long expiredSubscriptions;
    private long totalChurned;
    private String methodology;
    private List<MonthlyChurnDto> churnByMonth;
    private List<PlanChurnDto> churnByPlan;
}
