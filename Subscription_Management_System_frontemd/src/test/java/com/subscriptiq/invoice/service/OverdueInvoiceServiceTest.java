package com.subscriptiq.invoice.service;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OverdueInvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    private OverdueInvoiceService overdueInvoiceService;

    @BeforeEach
    void setUp() {
        overdueInvoiceService = new OverdueInvoiceServiceImpl(invoiceRepository);
    }

    @Test
    @DisplayName("Should mark ISSUED invoice overdue when due date has passed and balance > 0")
    void shouldMarkIssuedInvoiceOverdue() {
        LocalDate today = LocalDate.of(2026, 3, 10);
        Invoice invoice = Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-001")
                .status(InvoiceStatus.ISSUED)
                .dueDate(LocalDate.of(2026, 3, 5))
                .balanceAmount(new BigDecimal("150.00"))
                .build();

        when(invoiceRepository.findOverdueCandidates(any(), any())).thenReturn(List.of(invoice));

        int updated = overdueInvoiceService.processOverdueInvoices(today);

        assertThat(updated).isEqualTo(1);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.OVERDUE);
        verify(invoiceRepository).save(invoice);
    }

    @Test
    @DisplayName("Should mark PARTIALLY_PAID invoice overdue when due date has passed and balance > 0")
    void shouldMarkPartiallyPaidInvoiceOverdue() {
        LocalDate today = LocalDate.of(2026, 3, 10);
        Invoice invoice = Invoice.builder()
                .id(2L)
                .invoiceNumber("INV-002")
                .status(InvoiceStatus.PARTIALLY_PAID)
                .dueDate(LocalDate.of(2026, 3, 8))
                .paidAmount(new BigDecimal("50.00"))
                .balanceAmount(new BigDecimal("100.00"))
                .build();

        when(invoiceRepository.findOverdueCandidates(any(), any())).thenReturn(List.of(invoice));

        int updated = overdueInvoiceService.processOverdueInvoices(today);

        assertThat(updated).isEqualTo(1);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.OVERDUE);
        verify(invoiceRepository).save(invoice);
    }

    @Test
    @DisplayName("Should never mark PAID or CANCELLED invoices overdue")
    void shouldNotMarkPaidOrCancelledOverdue() {
        LocalDate today = LocalDate.of(2026, 3, 10);
        Invoice paid = Invoice.builder()
                .id(3L)
                .invoiceNumber("INV-PAID")
                .status(InvoiceStatus.PAID)
                .dueDate(LocalDate.of(2026, 3, 5))
                .balanceAmount(BigDecimal.ZERO)
                .build();

        Invoice cancelled = Invoice.builder()
                .id(4L)
                .invoiceNumber("INV-CANCELLED")
                .status(InvoiceStatus.CANCELLED)
                .dueDate(LocalDate.of(2026, 3, 5))
                .balanceAmount(new BigDecimal("100.00"))
                .build();

        when(invoiceRepository.findOverdueCandidates(any(), any())).thenReturn(List.of(paid, cancelled));

        int updated = overdueInvoiceService.processOverdueInvoices(today);

        assertThat(updated).isEqualTo(0);
        assertThat(paid.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(cancelled.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
        verify(invoiceRepository, never()).save(any());
    }
}

