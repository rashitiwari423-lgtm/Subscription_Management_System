package com.subscriptiq.scheduler.service;

import com.subscriptiq.invoice.service.OverdueInvoiceService;
import com.subscriptiq.scheduler.dto.BillingRunSummary;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringBillingServiceImpl implements RecurringBillingService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionBillingProcessor billingProcessor;
    private final OverdueInvoiceService overdueInvoiceService;

    @Override
    public BillingRunSummary runRecurringBilling() {
        return runRecurringBilling(LocalDate.now());
    }

    @Override
    public BillingRunSummary runRecurringBilling(LocalDate executionDate) {
        LocalDate runDate = executionDate != null ? executionDate : LocalDate.now();
        log.info("Starting recurring billing execution for date [{}]", runDate);

        // 1. Process overdue invoices first
        int overdueUpdated = overdueInvoiceService.processOverdueInvoices(runDate);

        // 2. Find eligible active subscriptions due for billing
        List<Subscription> eligibleSubscriptions = subscriptionRepository.findEligibleForBilling(
                SubscriptionStatus.ACTIVE, runDate);

        int subscriptionsChecked = eligibleSubscriptions.size();
        int invoicesCreated = 0;
        int alreadyProcessed = 0;
        int failed = 0;

        log.info("Found {} active subscriptions eligible for recurring billing check on [{}]",
                subscriptionsChecked, runDate);

        for (Subscription sub : eligibleSubscriptions) {
            try {
                SubscriptionBillingProcessor.ProcessResult result = billingProcessor.processSingleSubscription(
                        sub.getId(), runDate);

                switch (result) {
                    case CREATED -> invoicesCreated++;
                    case DUPLICATE_SKIPPED -> alreadyProcessed++;
                    case INACTIVE_SKIPPED, NOT_DUE_SKIPPED -> {
                        // Considered as already handled or no action required
                    }
                }
            } catch (Exception ex) {
                log.error("Failed to process billing for subscription id [{}] (error: {})",
                        sub.getId(), ex.getMessage());
                failed++;
            }
        }

        BillingRunSummary summary = BillingRunSummary.builder()
                .subscriptionsChecked(subscriptionsChecked)
                .invoicesCreated(invoicesCreated)
                .alreadyProcessed(alreadyProcessed)
                .failed(failed)
                .overdueInvoicesUpdated(overdueUpdated)
                .build();

        log.info("Completed recurring billing execution for [{}]: checked={}, created={}, skipped={}, failed={}, overdueUpdated={}",
                runDate, subscriptionsChecked, invoicesCreated, alreadyProcessed, failed, overdueUpdated);

        return summary;
    }
}

