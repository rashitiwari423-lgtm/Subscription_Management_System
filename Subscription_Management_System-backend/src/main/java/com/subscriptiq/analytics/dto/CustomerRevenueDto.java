package com.subscriptiq.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRevenueDto {

    private Long customerId;
    private String companyName;
    private BigDecimal billedRevenue;
    private BigDecimal paidRevenue;
}
