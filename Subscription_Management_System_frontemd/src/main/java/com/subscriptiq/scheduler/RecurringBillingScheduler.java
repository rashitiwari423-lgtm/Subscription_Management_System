package com.subscriptiq.scheduler;

import com.subscriptiq.config.BillingProperties;
import com.subscriptiq.scheduler.dto.BillingRunSummary;
import com.subscriptiq.scheduler.service.RecurringBillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecurringBillingScheduler {

    private final RecurringBillingService recurringBillingService;
    private final BillingProperties billingProperties;

    @Scheduled(cron = "${subscriptiq.billing.scheduler.cron:0 0 1 * * *}")
    public void scheduleRecurringBilling() {
        if (!billingProperties.getScheduler().isEnabled()) {
            log.debug("Recurring billing scheduler is disabled via configuration. Skipping scheduled run.");
            return;
        }

        log.info("Scheduled recurring billing job triggered at [{}]", LocalDate.now());
        try {
            BillingRunSummary summary = recurringBillingService.runRecurringBilling(LocalDate.now());
            log.info("Scheduled recurring billing job completed successfully. Summary: {}", summary);
        } catch (Exception ex) {
            log.error("Scheduled recurring billing job encountered an unexpected error: {}", ex.getMessage(), ex);
        }
    }
}

