package com.subscriptiq.ai.action.executor;

import com.subscriptiq.ai.action.dto.ActionExecutionResult;
import com.subscriptiq.ai.action.dto.ActionPreviewDto;
import com.subscriptiq.ai.action.entity.AiActionRequest;
import com.subscriptiq.ai.action.enums.AiActionExecutionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import com.subscriptiq.ai.entity.AiInsight;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import com.subscriptiq.invoice.repository.InvoiceRepository;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.user.entity.User;
import com.subscriptiq.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OverdueCustomerFollowUpExecutorTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private OverdueCustomerFollowUpExecutor executor;

    private UserPrincipal adminPrincipal;
    private Invoice overdueInvoice;

    @BeforeEach
    void setUp() {
        User adminUser = User.builder()
                .id(1L)
                .email("admin@test.com")
                .role(UserRole.ADMIN)
                .build();
        adminPrincipal = UserPrincipal.create(adminUser);

        Customer customer = Customer.builder()
                .id(200L)
                .companyName("Late Corp")
                .email("billing@latecorp.com")
                .build();

        overdueInvoice = Invoice.builder()
                .id(101L)
                .invoiceNumber("INV-2026-999")
                .customer(customer)
                .status(InvoiceStatus.OVERDUE)
                .totalAmount(new BigDecimal("500.00"))
                .balanceAmount(new BigDecimal("500.00"))
                .dueDate(LocalDate.now().minusDays(5))
                .build();
    }

    @Test
    @DisplayName("getActionType - Returns FOLLOW_UP_OVERDUE_CUSTOMERS")
    void testGetActionType() {
        assertThat(executor.getActionType()).isEqualTo(AiActionType.FOLLOW_UP_OVERDUE_CUSTOMERS);
    }

    @Test
    @DisplayName("preview - Correctly identifies overdue invoices and balances")
    void testPreview() {
        when(invoiceRepository.findByStatus(InvoiceStatus.OVERDUE))
                .thenReturn(List.of(overdueInvoice));
        when(invoiceRepository.findOverdueCandidates(any(), any()))
                .thenReturn(Collections.emptyList());

        AiInsight insight = AiInsight.builder().id(2L).build();
        ActionPreviewDto preview = executor.preview(insight);

        assertThat(preview.getAffectedCustomers()).isEqualTo(1);
        assertThat(preview.getAffectedInvoices()).isEqualTo(1);
        assertThat(preview.getPotentialRevenue()).isEqualByComparingTo("500.00");
        assertThat(preview.getAffectedResources()).hasSize(1);
    }

    @Test
    @DisplayName("execute - Generates follow-up notices without altering invoice status or amounts")
    void testExecute() {
        when(invoiceRepository.findByStatus(InvoiceStatus.OVERDUE))
                .thenReturn(List.of(overdueInvoice));
        when(invoiceRepository.findOverdueCandidates(any(), any()))
                .thenReturn(Collections.emptyList());

        AiActionRequest action = AiActionRequest.builder()
                .id(20L)
                .actionType(AiActionType.FOLLOW_UP_OVERDUE_CUSTOMERS)
                .build();

        ActionExecutionResult result = executor.execute(action, adminPrincipal);

        assertThat(result.getStatus()).isEqualTo(AiActionExecutionStatus.SUCCESS);
        assertThat(result.getAffectedCount()).isEqualTo(1);
        assertThat(result.getSummary()).contains("Follow-up notices generated for 1 overdue customers");

        // Verify invoice was not mutated
        assertThat(overdueInvoice.getStatus()).isEqualTo(InvoiceStatus.OVERDUE);
        assertThat(overdueInvoice.getBalanceAmount()).isEqualByComparingTo("500.00");
    }
}
