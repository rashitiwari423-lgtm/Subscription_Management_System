package com.subscriptiq.invoice.service;

import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.entity.InvoiceItem;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface BillingCalculationService {

    BigDecimal calculateItemSubtotal(Integer quantity, BigDecimal unitPrice);

    BigDecimal calculateItemTotal(Integer quantity, BigDecimal unitPrice, BigDecimal discountAmount, BigDecimal taxAmount);

    void recalculateInvoiceTotals(Invoice invoice);

    boolean isOverdue(Invoice invoice, LocalDate currentDate);

    void updateInvoiceStatus(Invoice invoice, LocalDate currentDate);
}

