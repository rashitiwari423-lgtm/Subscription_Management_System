package com.subscriptiq.analytics.dto;

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
public class PaymentAnalyticsResponse {

    private long totalPayments;
    private BigDecimal totalCollected;
    private long successfulPayments;
    private long pendingPayments;
    private long failedPayments;
    private long refundedPayments;
    private BigDecimal overdueInvoiceAmount;
    private Double paymentSuccessRate;
    private List<MonthlyAmountDto> monthlyPaymentCollection;
}
