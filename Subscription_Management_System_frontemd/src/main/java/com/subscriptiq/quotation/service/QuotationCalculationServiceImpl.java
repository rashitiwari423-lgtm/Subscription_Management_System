package com.subscriptiq.quotation.service;

import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.plan.enums.BillingCycle;
import com.subscriptiq.quotation.entity.Quotation;
import com.subscriptiq.quotation.entity.QuotationItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class QuotationCalculationServiceImpl implements QuotationCalculationService {

    @Override
    public BigDecimal calculateItemSubtotal(Integer quantity, BigDecimal unitPrice) {
        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Unit price cannot be null or negative");
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateItemTotal(Integer quantity, BigDecimal unitPrice, BigDecimal discountAmount, BigDecimal taxAmount) {
        BigDecimal subtotal = calculateItemSubtotal(quantity, unitPrice);
        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        BigDecimal tax = taxAmount != null ? taxAmount : BigDecimal.ZERO;

        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Discount amount cannot be negative");
        }
        if (tax.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Tax amount cannot be negative");
        }

        BigDecimal netTotal = subtotal.subtract(discount).add(tax).setScale(2, RoundingMode.HALF_UP);
        return netTotal.max(BigDecimal.ZERO);
    }

    @Override
    public void recalculateQuotation(Quotation quotation) {
        if (quotation == null || quotation.getItems() == null || quotation.getItems().isEmpty()) {
            throw new BadRequestException("Quotation must have at least one line item to calculate totals");
        }

        BigDecimal subtotalSum = BigDecimal.ZERO;
        BigDecimal discountSum = BigDecimal.ZERO;
        BigDecimal taxSum = BigDecimal.ZERO;
        BigDecimal totalSum = BigDecimal.ZERO;

        for (QuotationItem item : quotation.getItems()) {
            BigDecimal itemSubtotal = calculateItemSubtotal(item.getQuantity(), item.getUnitPrice());
            BigDecimal itemDiscount = item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal itemTax = item.getTaxAmount() != null ? item.getTaxAmount() : BigDecimal.ZERO;
            BigDecimal itemTotal = calculateItemTotal(item.getQuantity(), item.getUnitPrice(), itemDiscount, itemTax);

            item.setDiscountAmount(itemDiscount);
            item.setTaxAmount(itemTax);
            item.setTotalAmount(itemTotal);

            subtotalSum = subtotalSum.add(itemSubtotal);
            discountSum = discountSum.add(itemDiscount);
            taxSum = taxSum.add(itemTax);
            totalSum = totalSum.add(itemTotal);
        }

        quotation.setSubtotal(subtotalSum.setScale(2, RoundingMode.HALF_UP));
        quotation.setDiscountAmount(discountSum.setScale(2, RoundingMode.HALF_UP));
        quotation.setTaxAmount(taxSum.setScale(2, RoundingMode.HALF_UP));
        quotation.setTotalAmount(totalSum.setScale(2, RoundingMode.HALF_UP));
    }

    @Override
    public LocalDate calculateNextBillingDate(LocalDate startDate, BillingCycle billingCycle) {
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (billingCycle == null) {
            return startDate.plusMonths(1);
        }

        return switch (billingCycle) {
            case MONTHLY -> startDate.plusMonths(1);
            case QUARTERLY -> startDate.plusMonths(3);
            case YEARLY -> startDate.plusYears(1);
        };
    }
}

