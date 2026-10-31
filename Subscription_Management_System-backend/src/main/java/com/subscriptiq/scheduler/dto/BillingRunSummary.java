package com.subscriptiq.scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingRunSummary {

    private int subscriptionsChecked;
    private int invoicesCreated;
    private int alreadyProcessed;
    private int failed;
    private int overdueInvoicesUpdated;
}

