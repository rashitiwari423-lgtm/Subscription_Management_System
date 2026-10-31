package com.subscriptiq.analytics.service;

import com.subscriptiq.analytics.dto.RevenueForecastResponse;
import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.repository.InvoiceRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevenueForecastServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private RevenueForecastServiceImpl revenueForecastService;

    @Test
    @DisplayName("Should handle empty invoice database with insufficient data response")
    void testEmptyDatabaseInsufficientData() {
        when(invoiceRepository.findAll()).thenReturn(Collections.emptyList());

        RevenueForecastResponse response = revenueForecastService.generateForecast(3, "RECENT_MONTHLY_AVERAGE");

        assertThat(response.isSufficientData()).isFalse();
        assertThat(response.getConfidence()).isEqualTo("INSUFFICIENT_DATA");
        assertThat(response.getForecast()).isEmpty();
        assertThat(response.getHistoricalPeriodMonths()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should generate arithmetic average forecast for RECENT_MONTHLY_AVERAGE")
    void testRecentMonthlyAverageForecast() {
        Invoice inv1 = Invoice.builder()
                .invoiceDate(LocalDate.of(2026, 1, 15))
                .totalAmount(new BigDecimal("1000.00"))
                .build();
        Invoice inv2 = Invoice.builder()
                .invoiceDate(LocalDate.of(2026, 2, 15))
                .totalAmount(new BigDecimal("2000.00"))
                .build();
        Invoice inv3 = Invoice.builder()
                .invoiceDate(LocalDate.of(2026, 3, 15))
                .totalAmount(new BigDecimal("3000.00"))
                .build();

        when(invoiceRepository.findAll()).thenReturn(List.of(inv1, inv2, inv3));

        RevenueForecastResponse response = revenueForecastService.generateForecast(3, "RECENT_MONTHLY_AVERAGE");

        assertThat(response.isSufficientData()).isTrue();
        assertThat(response.getConfidence()).isEqualTo("BASELINE");
        assertThat(response.getHistoricalPeriodMonths()).isEqualTo(3);
        // Average of 1000, 2000, 3000 is 2000.00
        assertThat(response.getAverageMonthlyRevenue()).isEqualByComparingTo("2000.00");
        assertThat(response.getForecast()).hasSize(3);
        assertThat(response.getForecast().get(0).getProjectedRevenue()).isEqualByComparingTo("2000.00");
    }

    @Test
    @DisplayName("Should generate weighted average forecast for WEIGHTED_RECENT_AVERAGE")
    void testWeightedRecentAverageForecast() {
        // Month 1 (weight 1): 1000
        // Month 2 (weight 2): 2000
        // Total weight = 1 + 2 = 3. Weighted sum = (1000*1 + 2000*2) = 5000. Weighted avg = 5000 / 3 = 1666.67
        Invoice inv1 = Invoice.builder()
                .invoiceDate(LocalDate.of(2026, 1, 15))
                .totalAmount(new BigDecimal("1000.00"))
                .build();
        Invoice inv2 = Invoice.builder()
                .invoiceDate(LocalDate.of(2026, 2, 15))
                .totalAmount(new BigDecimal("2000.00"))
                .build();

        when(invoiceRepository.findAll()).thenReturn(List.of(inv1, inv2));

        RevenueForecastResponse response = revenueForecastService.generateForecast(2, "WEIGHTED_RECENT_AVERAGE");

        assertThat(response.isSufficientData()).isTrue();
        assertThat(response.getAverageMonthlyRevenue()).isEqualByComparingTo("1666.67");
        assertThat(response.getForecast()).hasSize(2);
    }

    @Test
    @DisplayName("Should reject invalid forecast month count")
    void testInvalidMonthCount() {
        assertThatThrownBy(() -> revenueForecastService.generateForecast(0, "RECENT_MONTHLY_AVERAGE"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("between 1 and 24");

        assertThatThrownBy(() -> revenueForecastService.generateForecast(25, "RECENT_MONTHLY_AVERAGE"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("between 1 and 24");
    }

    @Test
    @DisplayName("Should reject unsupported forecasting methodology")
    void testUnsupportedMethodology() {
        assertThatThrownBy(() -> revenueForecastService.generateForecast(3, "DEEP_LEARNING_LSTM"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Unsupported forecasting methodology");
    }
}
