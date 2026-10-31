package com.subscriptiq.scheduler.service;

import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.config.BillingProperties;
import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.entity.InvoiceItem;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import com.subscriptiq.invoice.repository.InvoiceRepository;
import com.subscriptiq.invoice.service.BillingCalculationService;
import com.subscriptiq.plan.enums.BillingCycle;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionBillingProcessorImpl implements SubscriptionBillingProcessor {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final BillingCalculationService calculationService;
    private final BillingProperties billingProperties;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProcessResult processSingleSubscription(Long subscriptionId, LocalDate executionDate) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            log.info("Subscription [{}] is not ACTIVE (status: {}). Skipping.",
                    subscription.getSubscriptionNumber(), subscription.getStatus());
            return ProcessResult.INACTIVE_SKIPPED;
        }

        if (subscription.getNextBillingDate() == null || subscription.getNextBillingDate().isAfter(executionDate)) {
            log.info("Subscription [{}] next billing date [{}] is in the future relative to execution date [{}]. Skipping.",
                    subscription.getSubscriptionNumber(), subscription.getNextBillingDate(), executionDate);
            return ProcessResult.NOT_DUE_SKIPPED;
        }

        LocalDate periodStart = subscription.getNextBillingDate();
        LocalDate periodEnd = calculateNextBillingDate(periodStart, subscription.getPlan().getBillingCycle());

        // 1. Application-level duplicate check
        if (invoiceRepository.existsBySubscriptionIdAndBillingPeriodStart(subscription.getId(), periodStart)) {
            log.info("Duplicate invoice skipped: Subscription [{}] already has an invoice for period start [{}].",
                    subscription.getSubscriptionNumber(), periodStart);
            return ProcessResult.DUPLICATE_SKIPPED;
        }

        int dueDays = billingProperties.getInvoice().getDueDays() > 0 ? billingProperties.getInvoice().getDueDays() : 7;
        LocalDate dueDate = executionDate.plusDays(dueDays);
        String invoiceNumber = generateUniqueInvoiceNumber(executionDate);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .customer(subscription.getCustomer())
                .subscription(subscription)
                .invoiceDate(executionDate)
                .billingPeriodStart(periodStart)
                .billingPeriodEnd(periodEnd)
                .dueDate(dueDate)
                .status(InvoiceStatus.ISSUED)
                .notes("Recurring billing for subscription " + subscription.getSubscriptionNumber()
                        + " (Period: " + periodStart + " to " + periodEnd + ")")
                .items(new ArrayList<>())
                .build();

        InvoiceItem item = InvoiceItem.builder()
                .plan(subscription.getPlan())
                .description("Subscription: " + subscription.getPlan().getName())
                .quantity(subscription.getQuantity() != null ? subscription.getQuantity() : 1)
                .unitPrice(subscription.getUnitPrice() != null ? subscription.getUnitPrice() : BigDecimal.ZERO)
                .discountAmount(subscription.getDiscountAmount() != null ? subscription.getDiscountAmount() : BigDecimal.ZERO)
                .taxAmount(subscription.getTaxAmount() != null ? subscription.getTaxAmount() : BigDecimal.ZERO)
                .build();

        invoice.addItem(item);
        calculationService.recalculateInvoiceTotals(invoice);

        try {
            // Save invoice (triggers DB-level uniqueness constraint if duplicate)
            invoiceRepository.saveAndFlush(invoice);

            // Update next billing date
            subscription.setNextBillingDate(periodEnd);
            subscriptionRepository.saveAndFlush(subscription);

            log.info("Successfully billed subscription [{}]: Created invoice [{}] (Period: {} to {}, Next billing: {})",
                    subscription.getSubscriptionNumber(), invoice.getInvoiceNumber(), periodStart, periodEnd, periodEnd);
            return ProcessResult.CREATED;

        } catch (DataIntegrityViolationException ex) {
            log.warn("Database uniqueness constraint caught concurrent duplicate invoice for subscription [{}] period start [{}].",
                    subscription.getSubscriptionNumber(), periodStart);
            return ProcessResult.DUPLICATE_SKIPPED;
        }
    }

    @Override
    public LocalDate calculateNextBillingDate(LocalDate fromDate, BillingCycle cycle) {
        if (fromDate == null) {
            fromDate = LocalDate.now();
        }
        if (cycle == null) {
            return fromDate.plusMonths(1);
        }

        return switch (cycle) {
            case MONTHLY -> fromDate.plusMonths(1);
            case QUARTERLY -> fromDate.plusMonths(3);
            case YEARLY -> fromDate.plusYears(1);
        };
    }

    private String generateUniqueInvoiceNumber(LocalDate date) {
        String datePart = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        String number;
        int attempts = 0;
        do {
            int randomSuffix = 1000 + RANDOM.nextInt(9000);
            number = "INV-" + datePart + "-" + randomSuffix;
            attempts++;
            if (attempts > 50) {
                number = "INV-" + datePart + "-" + (System.currentTimeMillis() % 100000);
            }
        } while (invoiceRepository.existsByInvoiceNumber(number));
        return number;
    }
}

