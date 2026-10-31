package com.subscriptiq.quotation.service;

import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.plan.entity.Plan;
import com.subscriptiq.plan.enums.BillingCycle;
import com.subscriptiq.quotation.entity.Quotation;
import com.subscriptiq.quotation.entity.QuotationItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuotationCalculationServiceTest {

    private QuotationCalculationService calculationService;

    @BeforeEach
    void setUp() {
        calculationService = new QuotationCalculationServiceImpl();
    }

    @Test
    @DisplayName("Calculate line subtotal correctly")
    void testCalculateItemSubtotal() {
        BigDecimal subtotal = calculationService.calculateItemSubtotal(3, new BigDecimal("100.50"));
        assertEquals(new BigDecimal("301.50"), subtotal);
    }

    @Test
    @DisplayName("Calculate line total with discount and tax")
    void testCalculateItemTotal() {
        BigDecimal total = calculationService.calculateItemTotal(
                2,
                new BigDecimal("500.00"),
                new BigDecimal("50.00"),
                new BigDecimal("18.00")
        );
        // (500 * 2) - 50 + 18 = 968.00
        assertEquals(new BigDecimal("968.00"), total);
    }

    @Test
    @DisplayName("Negative discount or tax throws BadRequestException")
    void testNegativeDiscountThrows() {
        assertThrows(BadRequestException.class, () ->
                calculationService.calculateItemTotal(1, new BigDecimal("100.00"), new BigDecimal("-10.00"), BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Recalculate quotation aggregate totals across multiple items")
    void testRecalculateQuotation() {
        Plan plan1 = Plan.builder().id(1L).price(new BigDecimal("100.00")).build();
        Plan plan2 = Plan.builder().id(2L).price(new BigDecimal("250.00")).build();

        QuotationItem item1 = QuotationItem.builder()
                .plan(plan1)
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .discountAmount(new BigDecimal("20.00"))
                .taxAmount(new BigDecimal("10.00"))
                .build();

        QuotationItem item2 = QuotationItem.builder()
                .plan(plan2)
                .quantity(1)
                .unitPrice(new BigDecimal("250.00"))
                .discountAmount(new BigDecimal("30.00"))
                .taxAmount(new BigDecimal("15.00"))
                .build();

        Quotation quotation = Quotation.builder()
                .items(new ArrayList<>(List.of(item1, item2)))
                .build();

        calculationService.recalculateQuotation(quotation);

        // Item 1: (100 * 2) - 20 + 10 = 190.00
        // Item 2: (250 * 1) - 30 + 15 = 235.00
        // Subtotal: 200 + 250 = 450.00
        // Total discount: 50.00
        // Total tax: 25.00
        // Net Total: 450 - 50 + 25 = 425.00
        assertEquals(new BigDecimal("450.00"), quotation.getSubtotal());
        assertEquals(new BigDecimal("50.00"), quotation.getDiscountAmount());
        assertEquals(new BigDecimal("25.00"), quotation.getTaxAmount());
        assertEquals(new BigDecimal("425.00"), quotation.getTotalAmount());
    }

    @Test
    @DisplayName("Calculate next billing date for each billing cycle")
    void testCalculateNextBillingDate() {
        LocalDate baseDate = LocalDate.of(2026, 1, 15);

        LocalDate monthly = calculationService.calculateNextBillingDate(baseDate, BillingCycle.MONTHLY);
        assertEquals(LocalDate.of(2026, 2, 15), monthly);

        LocalDate quarterly = calculationService.calculateNextBillingDate(baseDate, BillingCycle.QUARTERLY);
        assertEquals(LocalDate.of(2026, 4, 15), quarterly);

        LocalDate yearly = calculationService.calculateNextBillingDate(baseDate, BillingCycle.YEARLY);
        assertEquals(LocalDate.of(2027, 1, 15), yearly);
    }
}

