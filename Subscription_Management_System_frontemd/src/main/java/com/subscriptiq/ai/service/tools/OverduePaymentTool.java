package com.subscriptiq.ai.service.tools;

import com.subscriptiq.ai.dto.CustomerSummaryDto;
import com.subscriptiq.ai.dto.OverduePaymentResult;
import com.subscriptiq.ai.enums.AiRecommendedAction;
import com.subscriptiq.ai.enums.AiSeverity;
import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import com.subscriptiq.invoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverduePaymentTool {

    private final InvoiceRepository invoiceRepository;

    public OverduePaymentResult analyze() {
        LocalDate today = LocalDate.now();
        List<Invoice> overdueInvoices = invoiceRepository.findByStatus(InvoiceStatus.OVERDUE);

        // Also check if any ISSUED/PARTIALLY_PAID invoices have passed due date:
        List<Invoice> overdueCandidates = invoiceRepository.findOverdueCandidates(
                List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID), today);

        List<Invoice> allOverdue = new ArrayList<>(overdueInvoices);
        for (Invoice candidate : overdueCandidates) {
            if (!allOverdue.contains(candidate)) {
                allOverdue.add(candidate);
            }
        }

        int count = allOverdue.size();
        BigDecimal totalOverdue = BigDecimal.ZERO;
        LocalDate oldestDueDate = null;
        String oldestInvoiceNumber = null;
        List<CustomerSummaryDto> affectedCustomers = new ArrayList<>();

        if (!allOverdue.isEmpty()) {
            allOverdue.sort(Comparator.comparing(Invoice::getDueDate));
            Invoice oldest = allOverdue.get(0);
            oldestDueDate = oldest.getDueDate();
            oldestInvoiceNumber = oldest.getInvoiceNumber();

            for (Invoice inv : allOverdue) {
                BigDecimal balance = inv.getBalanceAmount() != null ? inv.getBalanceAmount() : inv.getTotalAmount();
                totalOverdue = totalOverdue.add(balance);

                long daysOverdue = ChronoUnit.DAYS.between(inv.getDueDate(), today);
                affectedCustomers.add(CustomerSummaryDto.builder()
                        .customerId(inv.getCustomer() != null ? inv.getCustomer().getId() : null)
                        .companyName(inv.getCustomer() != null ? inv.getCustomer().getCompanyName() : "Unknown")
                        .email(inv.getCustomer() != null ? inv.getCustomer().getEmail() : null)
                        .amount(balance)
                        .detail(String.format("Invoice %s (%d days overdue)", inv.getInvoiceNumber(), Math.max(0, daysOverdue)))
                        .build());
            }
        }

        AiSeverity severity;
        if (count == 0) {
            severity = AiSeverity.INFO;
        } else if (totalOverdue.compareTo(new BigDecimal("10000.00")) >= 0 || count >= 5) {
            severity = AiSeverity.CRITICAL;
        } else {
            severity = AiSeverity.WARNING;
        }

        return OverduePaymentResult.builder()
                .count(count)
                .totalAmount(totalOverdue)
                .oldestDueDate(oldestDueDate)
                .oldestInvoiceNumber(oldestInvoiceNumber)
                .severity(severity)
                .recommendedAction(AiRecommendedAction.FOLLOW_UP_OVERDUE_CUSTOMERS)
                .topAffectedCustomers(affectedCustomers)
                .build();
    }
}
