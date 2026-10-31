package com.subscriptiq.invoice.service;

import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.entity.InvoiceItem;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class BillingCalculationServiceTest {

    private BillingCalculationService calculationService;

    @BeforeEach
    void setUp() {
        calculationService = new BillingCalculationServiceImpl();
    }

    @Test
    @DisplayName("Should correctly calculate item subtotal")
    void shouldCalculateItemSubtotal() {
        BigDecimal subtotal = calculationService.calculateItemSubtotal(3, new BigDecimal("150.00"));
        assertThat(subtotal).isEqualByComparingTo(new BigDecimal("450.00"));
    }

    @Test
    @DisplayName("Should correctly calculate item total with discount and tax")
    void shouldCalculateItemTotal() {
        // subtotal = 2 * 100 = 200
        // discount = 20 -> taxable = 180
        // tax = 18 -> total = 198
        BigDecimal total = calculationService.calculateItemTotal(
                2,
                new BigDecimal("100.00"),
                new BigDecimal("20.00"),
                new BigDecimal("18.00")
        );
        assertThat(total).isEqualByComparingTo(new BigDecimal("198.00"));
    }

    @Test
    @DisplayName("Should handle discount exceeding subtotal by clamping taxable amount to 0")
    void shouldClampTaxableAmountToZero() {
        BigDecimal total = calculationService.calculateItemTotal(
                1,
                new BigDecimal("50.00"),
                new BigDecimal("80.00"),
                new BigDecimal("5.00")
        );
        assertThat(total).isEqualByComparingTo(new BigDecimal("5.00"));
    }

    @Test
    @DisplayName("Should recalculate invoice totals and balance accurately")
    void shouldRecalculateInvoiceTotals() {
        Invoice invoice = Invoice.builder()
                .items(new ArrayList<>())
                .paidAmount(new BigDecimal("50.00"))
                .build();

        InvoiceItem item1 = InvoiceItem.builder()
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .discountAmount(new BigDecimal("10.00"))
                .taxAmount(new BigDecimal("19.00"))
                .build();

        InvoiceItem item2 = InvoiceItem.builder()
                .quantity(1)
                .unitPrice(new BigDecimal("200.00"))
                .discountAmount(new BigDecimal("20.00"))
                .taxAmount(new BigDecimal("18.00"))
                .build();

        invoice.addItem(item1);
        invoice.addItem(item2);

        calculationService.recalculateInvoiceTotals(invoice);

        // Subtotal: 200 + 200 = 400.00
        assertThat(invoice.getSubtotal()).isEqualByComparingTo(new BigDecimal("400.00"));
        // Total discount: 10 + 20 = 30.00
        assertThat(invoice.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
        // Total tax: 19 + 18 = 37.00
        assertThat(invoice.getTaxAmount()).isEqualByComparingTo(new BigDecimal("37.00"));
        // Total: (200 - 10 + 19) + (200 - 20 + 18) = 209 + 198 = 407.00
        assertThat(invoice.getTotalAmount()).isEqualByComparingTo(new BigDecimal("407.00"));
        // Paid: 50.00
        assertThat(invoice.getPaidAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        // Balance: 407.00 - 50.00 = 357.00
        assertThat(invoice.getBalanceAmount()).isEqualByComparingTo(new BigDecimal("357.00"));
    }

    @Test
    @DisplayName("Should identify overdue invoice when due date is past and balance remains")
    void shouldIdentifyOverdueInvoice() {
        Invoice invoice = Invoice.builder()
                .dueDate(LocalDate.now().minusDays(2))
                .balanceAmount(new BigDecimal("100.00"))
                .status(InvoiceStatus.ISSUED)
                .build();

        boolean overdue = calculationService.isOverdue(invoice, LocalDate.now());
        assertThat(overdue).isTrue();
    }

    @Test
    @DisplayName("Paid or cancelled invoices should never be marked overdue")
    void paidOrCancelledShouldNotBeOverdue() {
        Invoice paidInvoice = Invoice.builder()
                .dueDate(LocalDate.now().minusDays(5))
                .balanceAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.PAID)
                .build();

        Invoice cancelledInvoice = Invoice.builder()
                .dueDate(LocalDate.now().minusDays(5))
                .balanceAmount(new BigDecimal("100.00"))
                .status(InvoiceStatus.CANCELLED)
                .build();

        assertThat(calculationService.isOverdue(paidInvoice, LocalDate.now())).isFalse();
        assertThat(calculationService.isOverdue(cancelledInvoice, LocalDate.now())).isFalse();
    }
}

