package com.subscriptiq.invoice.service;

import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import com.subscriptiq.invoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OverdueInvoiceServiceImpl implements OverdueInvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Override
    @Transactional
    public int processOverdueInvoices(LocalDate currentDate) {
        LocalDate checkDate = currentDate != null ? currentDate : LocalDate.now();

        List<InvoiceStatus> targetStatuses = List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID);
        List<Invoice> candidates = invoiceRepository.findOverdueCandidates(targetStatuses, checkDate);

        int count = 0;
        for (Invoice invoice : candidates) {
            // Extra sanity check: must not be PAID, must not be CANCELLED, balance > 0, dueDate < checkDate
            if (invoice.getStatus() != InvoiceStatus.PAID
                    && invoice.getStatus() != InvoiceStatus.CANCELLED
                    && invoice.getBalanceAmount() != null
                    && invoice.getBalanceAmount().compareTo(BigDecimal.ZERO) > 0
                    && invoice.getDueDate() != null
                    && invoice.getDueDate().isBefore(checkDate)) {

                invoice.setStatus(InvoiceStatus.OVERDUE);
                invoiceRepository.save(invoice);
                count++;
                log.info("Invoice [{}] marked OVERDUE (dueDate={}, balance={})",
                        invoice.getInvoiceNumber(), invoice.getDueDate(), invoice.getBalanceAmount());
            }
        }

        log.info("Overdue processing completed for date [{}]: {} invoices marked OVERDUE", checkDate, count);
        return count;
    }
}

