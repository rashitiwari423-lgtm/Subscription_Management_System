package com.subscriptiq.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanChurnDto {

    private Long planId;
    private String planName;
    private long totalSubscriptions;
    private long activeSubscriptions;
    private long churnedSubscriptions;
    private Double churnRate;
}
