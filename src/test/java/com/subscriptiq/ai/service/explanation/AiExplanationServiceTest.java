package com.subscriptiq.ai.service.explanation;

import com.subscriptiq.ai.dto.OverduePaymentResult;
import com.subscriptiq.ai.enums.AiIntent;
import com.subscriptiq.ai.enums.AiSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiExplanationServiceTest {

    @Mock
    private LlmProvider llmProvider;

    private AiExplanationService explanationService;

    @BeforeEach
    void setUp() {
        explanationService = new AiExplanationServiceImpl(llmProvider);
    }

    @Test
    @DisplayName("Deterministic fallback explanation when LLM is unavailable")
    void testExplain_DeterministicFallback() {
        when(llmProvider.isAvailable()).thenReturn(false);

        OverduePaymentResult result = OverduePaymentResult.builder()
                .count(3)
                .totalAmount(new BigDecimal("45000.00"))
                .oldestDueDate(LocalDate.of(2026, 9, 1))
                .oldestInvoiceNumber("INV-001")
                .severity(AiSeverity.CRITICAL)
                .build();

        String summary = explanationService.explain(AiIntent.OVERDUE_PAYMENTS, result);

        assertThat(summary).contains("3 overdue invoices totaling INR 45000.00");
        assertThat(summary).contains("INV-001");
    }

    @Test
    @DisplayName("LLM explanation is used when LLM provider is available")
    void testExplain_LlmProviderAvailable() {
        when(llmProvider.isAvailable()).thenReturn(true);
        when(llmProvider.generateExplanation(any(), any())).thenReturn("Executive Summary: Urgent overdue payments require immediate attention.");

        String summary = explanationService.explain(AiIntent.OVERDUE_PAYMENTS, new Object());

        assertThat(summary).isEqualTo("Executive Summary: Urgent overdue payments require immediate attention.");
    }

    @Test
    @DisplayName("Falls back to deterministic summary if LLM throws an unexpected exception")
    void testExplain_LlmThrowsExceptionFallback() {
        when(llmProvider.isAvailable()).thenReturn(true);
        when(llmProvider.generateExplanation(any(), any())).thenThrow(new RuntimeException("API timeout"));

        OverduePaymentResult result = OverduePaymentResult.builder()
                .count(0)
                .totalAmount(BigDecimal.ZERO)
                .build();

        String summary = explanationService.explain(AiIntent.OVERDUE_PAYMENTS, result);

        assertThat(summary).contains("No overdue payments found");
    }
}

