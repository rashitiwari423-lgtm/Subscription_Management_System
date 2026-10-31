package com.subscriptiq.quotation.service;

import com.subscriptiq.plan.enums.BillingCycle;
import com.subscriptiq.quotation.entity.Quotation;
import com.subscriptiq.quotation.entity.QuotationItem;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface QuotationCalculationService {

    BigDecimal calculateItemSubtotal(Integer quantity, BigDecimal unitPrice);

    BigDecimal calculateItemTotal(Integer quantity, BigDecimal unitPrice, BigDecimal discountAmount, BigDecimal taxAmount);

    void recalculateQuotation(Quotation quotation);

    LocalDate calculateNextBillingDate(LocalDate startDate, BillingCycle billingCycle);
}

