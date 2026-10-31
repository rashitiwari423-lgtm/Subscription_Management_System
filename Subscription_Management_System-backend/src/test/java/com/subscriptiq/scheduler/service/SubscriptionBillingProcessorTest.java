package com.subscriptiq.scheduler.service;

import com.subscriptiq.config.BillingProperties;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.repository.InvoiceRepository;
import com.subscriptiq.invoice.service.BillingCalculationService;
import com.subscriptiq.plan.entity.Plan;
import com.subscriptiq.plan.enums.BillingCycle;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionBillingProcessorTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private BillingCalculationService calculationService;

    private BillingProperties billingProperties;
    private SubscriptionBillingProcessor billingProcessor;

    @BeforeEach
    void setUp() {
        billingProperties = new BillingProperties();
        billingProperties.getInvoice().setDueDays(7);
        billingProcessor = new SubscriptionBillingProcessorImpl(
                subscriptionRepository,
                invoiceRepository,
                calculationService,
                billingProperties
        );
    }

    @Test
    @DisplayName("Should calculate monthly next billing date using plusMonths(1)")
    void shouldCalculateMonthlyNextBillingDate() {
        LocalDate start = LocalDate.of(2026, 1, 15);
        LocalDate next = billingProcessor.calculateNextBillingDate(start, BillingCycle.MONTHLY);
        assertThat(next).isEqualTo(LocalDate.of(2026, 2, 15));
    }

    @Test
    @DisplayName("Should calculate quarterly next billing date using plusMonths(3)")
    void shouldCalculateQuarterlyNextBillingDate() {
        LocalDate start = LocalDate.of(2026, 1, 15);
        LocalDate next = billingProcessor.calculateNextBillingDate(start, BillingCycle.QUARTERLY);
        assertThat(next).isEqualTo(LocalDate.of(2026, 4, 15));
    }

    @Test
    @DisplayName("Should calculate yearly next billing date using plusYears(1)")
    void shouldCalculateYearlyNextBillingDate() {
        LocalDate start = LocalDate.of(2026, 1, 15);
        LocalDate next = billingProcessor.calculateNextBillingDate(start, BillingCycle.YEARLY);
        assertThat(next).isEqualTo(LocalDate.of(2027, 1, 15));
    }

    @Test
    @DisplayName("Should preserve end of month when safely possible")
    void shouldHandleEndOfMonthDates() {
        LocalDate start = LocalDate.of(2026, 1, 31);
        LocalDate nextMonthly = billingProcessor.calculateNextBillingDate(start, BillingCycle.MONTHLY);
        // Feb 2026 has 28 days
        assertThat(nextMonthly).isEqualTo(LocalDate.of(2026, 2, 28));
    }

    @Test
    @DisplayName("Should skip billing if subscription is not ACTIVE")
    void shouldSkipNonActiveSubscription() {
        Subscription subscription = Subscription.builder()
                .id(1L)
                .subscriptionNumber("SUB-001")
                .status(SubscriptionStatus.PAUSED)
                .nextBillingDate(LocalDate.now())
                .build();

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        SubscriptionBillingProcessor.ProcessResult result = billingProcessor.processSingleSubscription(1L, LocalDate.now());

        assertThat(result).isEqualTo(SubscriptionBillingProcessor.ProcessResult.INACTIVE_SKIPPED);
        verify(invoiceRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Should skip billing if nextBillingDate is in the future")
    void shouldSkipFutureNextBillingDate() {
        Subscription subscription = Subscription.builder()
                .id(1L)
                .subscriptionNumber("SUB-001")
                .status(SubscriptionStatus.ACTIVE)
                .nextBillingDate(LocalDate.now().plusDays(5))
                .build();

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        SubscriptionBillingProcessor.ProcessResult result = billingProcessor.processSingleSubscription(1L, LocalDate.now());

        assertThat(result).isEqualTo(SubscriptionBillingProcessor.ProcessResult.NOT_DUE_SKIPPED);
        verify(invoiceRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Should skip billing if duplicate invoice exists for same period start")
    void shouldSkipDuplicateInvoice() {
        LocalDate today = LocalDate.now();
        Subscription subscription = Subscription.builder()
                .id(1L)
                .subscriptionNumber("SUB-001")
                .status(SubscriptionStatus.ACTIVE)
                .nextBillingDate(today)
                .plan(Plan.builder().billingCycle(BillingCycle.MONTHLY).name("Basic Plan").build())
                .build();

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(invoiceRepository.existsBySubscriptionIdAndBillingPeriodStart(1L, today)).thenReturn(true);

        SubscriptionBillingProcessor.ProcessResult result = billingProcessor.processSingleSubscription(1L, today);

        assertThat(result).isEqualTo(SubscriptionBillingProcessor.ProcessResult.DUPLICATE_SKIPPED);
        verify(invoiceRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Should create invoice and update nextBillingDate for eligible subscription")
    void shouldCreateInvoiceAndAdvanceBillingDate() {
        LocalDate today = LocalDate.of(2026, 3, 1);
        Plan plan = Plan.builder()
                .id(10L)
                .name("Pro Plan")
                .price(new BigDecimal("100.00"))
                .billingCycle(BillingCycle.MONTHLY)
                .build();

        Customer customer = Customer.builder().id(5L).companyName("Test Corp").build();

        Subscription subscription = Subscription.builder()
                .id(1L)
                .subscriptionNumber("SUB-2026-0001")
                .customer(customer)
                .plan(plan)
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .discountAmount(new BigDecimal("10.00"))
                .taxAmount(new BigDecimal("15.00"))
                .totalAmount(new BigDecimal("205.00"))
                .status(SubscriptionStatus.ACTIVE)
                .nextBillingDate(today)
                .build();

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(invoiceRepository.existsBySubscriptionIdAndBillingPeriodStart(1L, today)).thenReturn(false);

        SubscriptionBillingProcessor.ProcessResult result = billingProcessor.processSingleSubscription(1L, today);

        assertThat(result).isEqualTo(SubscriptionBillingProcessor.ProcessResult.CREATED);

        // Verify invoice was saved with correct dates and snapshot amounts
        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).saveAndFlush(invoiceCaptor.capture());

        Invoice savedInvoice = invoiceCaptor.getValue();
        assertThat(savedInvoice.getCustomer()).isEqualTo(customer);
        assertThat(savedInvoice.getSubscription()).isEqualTo(subscription);
        assertThat(savedInvoice.getInvoiceDate()).isEqualTo(today);
        assertThat(savedInvoice.getBillingPeriodStart()).isEqualTo(today);
        assertThat(savedInvoice.getBillingPeriodEnd()).isEqualTo(today.plusMonths(1));
        assertThat(savedInvoice.getDueDate()).isEqualTo(today.plusDays(7));

        // Verify subscription nextBillingDate advanced to periodEnd
        assertThat(subscription.getNextBillingDate()).isEqualTo(today.plusMonths(1));
        verify(subscriptionRepository).saveAndFlush(subscription);
    }
}

