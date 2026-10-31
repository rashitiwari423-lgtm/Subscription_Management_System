package com.subscriptiq.scheduler.service;

import com.subscriptiq.invoice.entity.Invoice;

import java.time.LocalDate;

public interface SubscriptionBillingProcessor {

    enum ProcessResult {
        CREATED,
        DUPLICATE_SKIPPED,
        NOT_DUE_SKIPPED,
        INACTIVE_SKIPPED
    }

    ProcessResult processSingleSubscription(Long subscriptionId, LocalDate executionDate);

    LocalDate calculateNextBillingDate(LocalDate fromDate, com.subscriptiq.plan.enums.BillingCycle cycle);
}

