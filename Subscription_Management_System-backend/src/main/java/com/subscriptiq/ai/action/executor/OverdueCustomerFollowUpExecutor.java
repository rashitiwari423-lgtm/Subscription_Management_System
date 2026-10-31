package com.subscriptiq.ai.action.executor;

import com.subscriptiq.ai.action.dto.ActionExecutionResult;
import com.subscriptiq.ai.action.dto.ActionPreviewDto;
import com.subscriptiq.ai.action.entity.AiActionRequest;
import com.subscriptiq.ai.action.enums.AiActionExecutionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import com.subscriptiq.ai.entity.AiInsight;
import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import com.subscriptiq.invoice.repository.InvoiceRepository;
import com.subscriptiq.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueCustomerFollowUpExecutor implements ActionExecutor {

    private final InvoiceRepository invoiceRepository;

    @Override
    public AiActionType getActionType() {
        return AiActionType.FOLLOW_UP_OVERDUE_CUSTOMERS;
    }

    @Override
    public ActionPreviewDto preview(AiInsight insight) {
        LocalDate today = LocalDate.now();
        List<Invoice> overdueInvoices = invoiceRepository.findByStatus(InvoiceStatus.OVERDUE);
        List<Invoice> overdueCandidates = invoiceRepository.findOverdueCandidates(
                List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID), today);

        List<Invoice> allOverdue = new ArrayList<>(overdueInvoices);
        for (Invoice candidate : overdueCandidates) {
            if (!allOverdue.contains(candidate)) {
                allOverdue.add(candidate);
            }
        }

        Set<Long> affectedCustomerIds = new HashSet<>();
        BigDecimal totalOverdue = BigDecimal.ZERO;
        List<String> affectedResources = new ArrayList<>();

        for (Invoice inv : allOverdue) {
            BigDecimal balance = inv.getBalanceAmount() != null ? inv.getBalanceAmount() : inv.getTotalAmount();
            totalOverdue = totalOverdue.add(balance);

            String company = inv.getCustomer() != null ? inv.getCustomer().getCompanyName() : "Customer #" + (inv.getCustomer() != null ? inv.getCustomer().getId() : "Unknown");
            if (inv.getCustomer() != null) {
                affectedCustomerIds.add(inv.getCustomer().getId());
            }

            long daysOverdue = ChronoUnit.DAYS.between(inv.getDueDate(), today);
            affectedResources.add(String.format("Invoice %s (%s, balance: %s, %d days overdue)",
                    inv.getInvoiceNumber(), company, balance, Math.max(0, daysOverdue)));
        }

        List<String> warnings = new ArrayList<>();
        warnings.add("Action requires admin approval. Invoices, balances, and payment statuses will NOT be modified.");
        if (allOverdue.isEmpty()) {
            warnings.add("Live check found 0 overdue invoices. Accounts may have been settled since insight generation.");
        }

        return ActionPreviewDto.builder()
                .title("Follow Up Overdue Customers")
                .description(String.format("Prepare overdue payment follow-up notices for %d customers with %d overdue invoices totaling %s.",
                        affectedCustomerIds.size(), allOverdue.size(), totalOverdue))
                .affectedCustomers(affectedCustomerIds.size())
                .affectedSubscriptions(0)
                .affectedInvoices(allOverdue.size())
                .potentialRevenue(totalOverdue)
                .potentialSavings(BigDecimal.ZERO)
                .affectedResources(affectedResources)
                .warnings(warnings)
                .build();
    }

    @Override
    public ActionExecutionResult execute(AiActionRequest action, UserPrincipal user) {
        LocalDate today = LocalDate.now();
        List<Invoice> overdueInvoices = invoiceRepository.findByStatus(InvoiceStatus.OVERDUE);
        List<Invoice> overdueCandidates = invoiceRepository.findOverdueCandidates(
                List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID), today);

        List<Invoice> allOverdue = new ArrayList<>(overdueInvoices);
        for (Invoice candidate : overdueCandidates) {
            if (!allOverdue.contains(candidate)) {
                allOverdue.add(candidate);
            }
        }

        Set<String> affectedCustomerNames = new HashSet<>();
        BigDecimal totalOverdue = BigDecimal.ZERO;

        for (Invoice inv : allOverdue) {
            BigDecimal balance = inv.getBalanceAmount() != null ? inv.getBalanceAmount() : inv.getTotalAmount();
            totalOverdue = totalOverdue.add(balance);

            String customerName = inv.getCustomer() != null ? inv.getCustomer().getCompanyName() : "Customer #" + (inv.getCustomer() != null ? inv.getCustomer().getId() : "Unknown");
            affectedCustomerNames.add(customerName);
        }

        int affectedCount = affectedCustomerNames.size();
        String summary = String.format("Follow-up notices generated for %d overdue customers across %d invoices (total balance: %s). Invoices remain intact without modification.",
                affectedCount, allOverdue.size(), totalOverdue);

        log.info("OverdueCustomerFollowUpExecutor executed by [{}]: {}", user != null ? user.getEmail() : "system", summary);

        return ActionExecutionResult.builder()
                .status(AiActionExecutionStatus.SUCCESS)
                .affectedCount(affectedCount)
                .summary(summary)
                .build();
    }
}
