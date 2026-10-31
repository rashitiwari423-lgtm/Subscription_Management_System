package com.subscriptiq.ai.action.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionPreviewDto {

    private String title;
    private String description;

    @Builder.Default
    private Integer affectedCustomers = 0;

    @Builder.Default
    private Integer affectedSubscriptions = 0;

    @Builder.Default
    private Integer affectedInvoices = 0;

    @Builder.Default
    private BigDecimal potentialRevenue = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal potentialSavings = BigDecimal.ZERO;

    @Builder.Default
    private List<String> affectedResources = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}

