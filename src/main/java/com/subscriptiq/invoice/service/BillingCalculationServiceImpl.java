package com.subscriptiq.invoice.service;

import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.entity.InvoiceItem;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class BillingCalculationServiceImpl implements BillingCalculationService {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    @Override
    public BigDecimal calculateItemSubtotal(Integer quantity, BigDecimal unitPrice) {
        if (quantity == null || quantity <= 0 || unitPrice == null) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(SCALE, ROUNDING);
    }

    @Override
    public BigDecimal calculateItemTotal(Integer quantity, BigDecimal unitPrice, BigDecimal discountAmount, BigDecimal taxAmount) {
        BigDecimal subtotal = calculateItemSubtotal(quantity, unitPrice);
        BigDecimal discount = discountAmount != null ? discountAmount.setScale(SCALE, ROUNDING) : BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        BigDecimal tax = taxAmount != null ? taxAmount.setScale(SCALE, ROUNDING) : BigDecimal.ZERO.setScale(SCALE, ROUNDING);

        BigDecimal taxableAmount = subtotal.subtract(discount);
        if (taxableAmount.compareTo(BigDecimal.ZERO) < 0) {
            taxableAmount = BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        }

        return taxableAmount.add(tax).setScale(SCALE, ROUNDING);
    }

    @Override
    public void recalculateInvoiceTotals(Invoice invoice) {
        if (invoice == null) {
            return;
        }

        BigDecimal subtotal = BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        BigDecimal discountTotal = BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        BigDecimal taxTotal = BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        BigDecimal totalAmount = BigDecimal.ZERO.setScale(SCALE, ROUNDING);

        if (invoice.getItems() != null) {
            for (InvoiceItem item : invoice.getItems()) {
                BigDecimal itemSubtotal = calculateItemSubtotal(item.getQuantity(), item.getUnitPrice());
                BigDecimal itemDiscount = item.getDiscountAmount() != null
                        ? item.getDiscountAmount().setScale(SCALE, ROUNDING)
                        : BigDecimal.ZERO.setScale(SCALE, ROUNDING);
                BigDecimal itemTax = item.getTaxAmount() != null
                        ? item.getTaxAmount().setScale(SCALE, ROUNDING)
                        : BigDecimal.ZERO.setScale(SCALE, ROUNDING);

                BigDecimal itemTotal = calculateItemTotal(item.getQuantity(), item.getUnitPrice(), itemDiscount, itemTax);

                item.setDiscountAmount(itemDiscount);
                item.setTaxAmount(itemTax);
                item.setTotalAmount(itemTotal);

                subtotal = subtotal.add(itemSubtotal);
                discountTotal = discountTotal.add(itemDiscount);
                taxTotal = taxTotal.add(itemTax);
                totalAmount = totalAmount.add(itemTotal);
            }
        }

        invoice.setSubtotal(subtotal);
        invoice.setDiscountAmount(discountTotal);
        invoice.setTaxAmount(taxTotal);
        invoice.setTotalAmount(totalAmount);

        BigDecimal paidAmount = invoice.getPaidAmount() != null
                ? invoice.getPaidAmount().setScale(SCALE, ROUNDING)
                : BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        invoice.setPaidAmount(paidAmount);

        BigDecimal balanceAmount = totalAmount.subtract(paidAmount).setScale(SCALE, ROUNDING);
        if (balanceAmount.compareTo(BigDecimal.ZERO) < 0) {
            balanceAmount = BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        }
        invoice.setBalanceAmount(balanceAmount);
    }

    @Override
    public boolean isOverdue(Invoice invoice, LocalDate currentDate) {
        if (invoice == null || invoice.getDueDate() == null || currentDate == null) {
            return false;
        }
        if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            return false;
        }
        boolean pastDue = currentDate.isAfter(invoice.getDueDate());
        boolean hasBalance = invoice.getBalanceAmount() != null && invoice.getBalanceAmount().compareTo(BigDecimal.ZERO) > 0;
        return pastDue && hasBalance;
    }

    @Override
    public void updateInvoiceStatus(Invoice invoice, LocalDate currentDate) {
        if (invoice == null || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            return;
        }

        BigDecimal balance = invoice.getBalanceAmount() != null ? invoice.getBalanceAmount() : BigDecimal.ZERO;
        BigDecimal paid = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal total = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;

        if (total.compareTo(BigDecimal.ZERO) > 0 && balance.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else if (paid.compareTo(BigDecimal.ZERO) > 0 && balance.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        } else if (isOverdue(invoice, currentDate)) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
        } else if (invoice.getStatus() == InvoiceStatus.OVERDUE && !isOverdue(invoice, currentDate)) {
            invoice.setStatus(InvoiceStatus.ISSUED);
        }
    }
}

