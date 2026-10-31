package com.subscriptiq.ai.service.tools;

import com.subscriptiq.ai.dto.OverduePaymentResult;
import com.subscriptiq.ai.enums.AiSeverity;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import com.subscriptiq.invoice.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class OverduePaymentToolTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    private OverduePaymentTool tool;

    @BeforeEach
    void setUp() {
        tool = new OverduePaymentTool(invoiceRepository);
    }

    @Test
    @DisplayName("Overdue payments analysis returns correct count, total amount and oldest due date")
    void testAnalyze_OverdueDetected() {
        Customer customer = Customer.builder().id(1L).companyName("Acme Corp").email("acme@test.com").build();
        Invoice invoice1 = Invoice.builder()
                .id(101L)
                .invoiceNumber("INV-101")
                .customer(customer)
                .status(InvoiceStatus.OVERDUE)
                .dueDate(LocalDate.of(2026, 9, 1))
                .balanceAmount(new BigDecimal("15000.00"))
                .build();

        Invoice invoice2 = Invoice.builder()
                .id(102L)
                .invoiceNumber("INV-102")
                .customer(customer)
                .status(InvoiceStatus.OVERDUE)
                .dueDate(LocalDate.of(2026, 9, 15))
                .balanceAmount(new BigDecimal("25000.00"))
                .build();

        when(invoiceRepository.findByStatus(InvoiceStatus.OVERDUE)).thenReturn(List.of(invoice1, invoice2));
        when(invoiceRepository.findOverdueCandidates(any(), any())).thenReturn(Collections.emptyList());

        OverduePaymentResult result = tool.analyze();

        assertThat(result.getCount()).isEqualTo(2);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("40000.00"));
        assertThat(result.getOldestDueDate()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(result.getOldestInvoiceNumber()).isEqualTo("INV-101");
        assertThat(result.getSeverity()).isEqualTo(AiSeverity.CRITICAL);
        assertThat(result.getTopAffectedCustomers()).hasSize(2);
    }

    @Test
    @DisplayName("Overdue analysis returns zero count and INFO severity when no invoices are overdue")
    void testAnalyze_NoOverdue() {
        when(invoiceRepository.findByStatus(InvoiceStatus.OVERDUE)).thenReturn(Collections.emptyList());
        when(invoiceRepository.findOverdueCandidates(any(), any())).thenReturn(Collections.emptyList());

        OverduePaymentResult result = tool.analyze();

        assertThat(result.getCount()).isEqualTo(0);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getSeverity()).isEqualTo(AiSeverity.INFO);
    }
}

