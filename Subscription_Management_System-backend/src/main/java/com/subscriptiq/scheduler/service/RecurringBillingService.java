package com.subscriptiq.scheduler.service;

import com.subscriptiq.scheduler.dto.BillingRunSummary;

import java.time.LocalDate;

public interface RecurringBillingService {

    BillingRunSummary runRecurringBilling();

    BillingRunSummary runRecurringBilling(LocalDate executionDate);
}

