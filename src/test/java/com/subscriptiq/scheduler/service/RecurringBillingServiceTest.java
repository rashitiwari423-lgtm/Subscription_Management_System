package com.subscriptiq.scheduler.service;

import com.subscriptiq.invoice.service.OverdueInvoiceService;
import com.subscriptiq.scheduler.dto.BillingRunSummary;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecurringBillingServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionBillingProcessor billingProcessor;

    @Mock
    private OverdueInvoiceService overdueInvoiceService;

    private RecurringBillingService recurringBillingService;

    @BeforeEach
    void setUp() {
        recurringBillingService = new RecurringBillingServiceImpl(
                subscriptionRepository,
                billingProcessor,
                overdueInvoiceService
        );
    }

    @Test
    @DisplayName("Should run overdue processing and bill eligible subscriptions")
    void shouldRunRecurringBilling() {
        LocalDate today = LocalDate.of(2026, 3, 15);
        Subscription s1 = Subscription.builder().id(1L).subscriptionNumber("SUB-001").status(SubscriptionStatus.ACTIVE).build();
        Subscription s2 = Subscription.builder().id(2L).subscriptionNumber("SUB-002").status(SubscriptionStatus.ACTIVE).build();

        when(overdueInvoiceService.processOverdueInvoices(today)).thenReturn(3);
        when(subscriptionRepository.findEligibleForBilling(SubscriptionStatus.ACTIVE, today)).thenReturn(List.of(s1, s2));
        when(billingProcessor.processSingleSubscription(1L, today)).thenReturn(SubscriptionBillingProcessor.ProcessResult.CREATED);
        when(billingProcessor.processSingleSubscription(2L, today)).thenReturn(SubscriptionBillingProcessor.ProcessResult.DUPLICATE_SKIPPED);

        BillingRunSummary summary = recurringBillingService.runRecurringBilling(today);

        assertThat(summary.getOverdueInvoicesUpdated()).isEqualTo(3);
        assertThat(summary.getSubscriptionsChecked()).isEqualTo(2);
        assertThat(summary.getInvoicesCreated()).isEqualTo(1);
        assertThat(summary.getAlreadyProcessed()).isEqualTo(1);
        assertThat(summary.getFailed()).isEqualTo(0);

        verify(overdueInvoiceService).processOverdueInvoices(today);
        verify(billingProcessor).processSingleSubscription(1L, today);
        verify(billingProcessor).processSingleSubscription(2L, today);
    }

    @Test
    @DisplayName("One failing subscription should not abort billing of other subscriptions")
    void shouldIsolateFailures() {
        LocalDate today = LocalDate.of(2026, 3, 15);
        Subscription s1 = Subscription.builder().id(1L).subscriptionNumber("SUB-001").status(SubscriptionStatus.ACTIVE).build();
        Subscription s2 = Subscription.builder().id(2L).subscriptionNumber("SUB-002").status(SubscriptionStatus.ACTIVE).build();

        when(overdueInvoiceService.processOverdueInvoices(today)).thenReturn(0);
        when(subscriptionRepository.findEligibleForBilling(SubscriptionStatus.ACTIVE, today)).thenReturn(List.of(s1, s2));
        when(billingProcessor.processSingleSubscription(1L, today)).thenThrow(new RuntimeException("Simulated database timeout"));
        when(billingProcessor.processSingleSubscription(2L, today)).thenReturn(SubscriptionBillingProcessor.ProcessResult.CREATED);

        BillingRunSummary summary = recurringBillingService.runRecurringBilling(today);

        assertThat(summary.getSubscriptionsChecked()).isEqualTo(2);
        assertThat(summary.getInvoicesCreated()).isEqualTo(1);
        assertThat(summary.getFailed()).isEqualTo(1);
        assertThat(summary.getAlreadyProcessed()).isEqualTo(0);
    }
}

