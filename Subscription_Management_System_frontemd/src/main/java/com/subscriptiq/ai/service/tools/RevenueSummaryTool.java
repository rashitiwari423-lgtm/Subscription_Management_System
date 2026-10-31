package com.subscriptiq.ai.service.tools;

import com.subscriptiq.ai.dto.RevenueSummaryResult;
import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import com.subscriptiq.invoice.repository.InvoiceRepository;
import com.subscriptiq.payment.entity.Payment;
import com.subscriptiq.payment.enums.PaymentStatus;
import com.subscriptiq.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevenueSummaryTool {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    public RevenueSummaryResult analyze() {
        List<Invoice> invoices = invoiceRepository.findAll();
        List<Payment> payments = paymentRepository.findByStatus(PaymentStatus.SUCCESS);

        BigDecimal totalInvoiced = BigDecimal.ZERO;
        BigDecimal totalCollected = BigDecimal.ZERO;
        int paidCount = 0;
        int overdueCount = 0;

        for (Invoice invoice : invoices) {
            BigDecimal total = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
            totalInvoiced = totalInvoiced.add(total);

            if (invoice.getStatus() == InvoiceStatus.PAID) {
                paidCount++;
            } else if (invoice.getStatus() == InvoiceStatus.OVERDUE) {
                overdueCount++;
            }
        }

        for (Payment payment : payments) {
            BigDecimal amt = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
            totalCollected = totalCollected.add(amt);
        }

        BigDecimal totalOutstanding = totalInvoiced.subtract(totalCollected);
        if (totalOutstanding.compareTo(BigDecimal.ZERO) < 0) {
            totalOutstanding = BigDecimal.ZERO;
        }

        BigDecimal collectionRate = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (totalInvoiced.compareTo(BigDecimal.ZERO) > 0) {
            collectionRate = totalCollected.divide(totalInvoiced, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return RevenueSummaryResult.builder()
                .totalInvoiced(totalInvoiced.setScale(2, RoundingMode.HALF_UP))
                .totalCollected(totalCollected.setScale(2, RoundingMode.HALF_UP))
                .totalOutstanding(totalOutstanding.setScale(2, RoundingMode.HALF_UP))
                .collectionRatePercentage(collectionRate)
                .totalInvoices(invoices.size())
                .paidInvoices(paidCount)
                .overdueInvoices(overdueCount)
                .build();
    }
}
