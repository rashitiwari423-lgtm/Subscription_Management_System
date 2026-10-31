package com.subscriptiq.analytics.service;

import com.subscriptiq.analytics.dto.ForecastMonthDto;
import com.subscriptiq.analytics.dto.HistoricalMonthlyRevenueDto;
import com.subscriptiq.analytics.dto.RevenueForecastResponse;
import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RevenueForecastServiceImpl implements RevenueForecastService {

    private final InvoiceRepository invoiceRepository;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public RevenueForecastResponse generateForecast(int months, String methodology) {
        if (months < 1 || months > 24) {
            throw new BadRequestException("Forecast months must be between 1 and 24");
        }

        String selectedMethodology = (methodology != null && !methodology.trim().isEmpty())
                ? methodology.trim().toUpperCase()
                : "RECENT_MONTHLY_AVERAGE";

        if (!selectedMethodology.equals("RECENT_MONTHLY_AVERAGE") && !selectedMethodology.equals("WEIGHTED_RECENT_AVERAGE")) {
            throw new BadRequestException("Unsupported forecasting methodology: " + methodology + ". Supported methodologies: RECENT_MONTHLY_AVERAGE, WEIGHTED_RECENT_AVERAGE");
        }

        List<Invoice> invoices = invoiceRepository.findAll();

        // Aggregate historical revenue by YearMonth
        Map<YearMonth, BigDecimal> monthlyTotals = new TreeMap<>();
        for (Invoice invoice : invoices) {
            if (invoice.getInvoiceDate() != null) {
                YearMonth ym = YearMonth.from(invoice.getInvoiceDate());
                BigDecimal amount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
                monthlyTotals.put(ym, monthlyTotals.getOrDefault(ym, BigDecimal.ZERO).add(amount));
            }
        }

        // Filter out months with zero revenue if entire history is empty
        List<Map.Entry<YearMonth, BigDecimal>> nonZeroMonths = monthlyTotals.entrySet().stream()
                .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        if (nonZeroMonths.isEmpty()) {
            return RevenueForecastResponse.builder()
                    .methodology(selectedMethodology)
                    .confidence("INSUFFICIENT_DATA")
                    .isSufficientData(false)
                    .message("Insufficient historical revenue data to generate a forecast. Requires at least 1 month with recorded revenue.")
                    .historicalPeriodMonths(0)
                    .averageMonthlyRevenue(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .historicalRevenue(Collections.emptyList())
                    .forecast(Collections.emptyList())
                    .forecastPeriod("N/A")
                    .build();
        }

        // Map to historical DTOs
        List<HistoricalMonthlyRevenueDto> historicalList = new ArrayList<>();
        for (Map.Entry<YearMonth, BigDecimal> entry : nonZeroMonths) {
            historicalList.add(HistoricalMonthlyRevenueDto.builder()
                    .month(entry.getKey().format(MONTH_FORMATTER))
                    .amount(entry.getValue().setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        int historicalCount = nonZeroMonths.size();
        BigDecimal projectedMonthlyRevenue;

        if ("WEIGHTED_RECENT_AVERAGE".equals(selectedMethodology)) {
            // Apply linear weights: 1, 2, ..., N for chronological entries
            BigDecimal weightedSum = BigDecimal.ZERO;
            long totalWeight = 0;
            int weight = 1;

            for (Map.Entry<YearMonth, BigDecimal> entry : nonZeroMonths) {
                weightedSum = weightedSum.add(entry.getValue().multiply(BigDecimal.valueOf(weight)));
                totalWeight += weight;
                weight++;
            }

            projectedMonthlyRevenue = weightedSum.divide(BigDecimal.valueOf(totalWeight), 2, RoundingMode.HALF_UP);
        } else {
            // RECENT_MONTHLY_AVERAGE: standard arithmetic mean
            BigDecimal sum = BigDecimal.ZERO;
            for (Map.Entry<YearMonth, BigDecimal> entry : nonZeroMonths) {
                sum = sum.add(entry.getValue());
            }
            projectedMonthlyRevenue = sum.divide(BigDecimal.valueOf(historicalCount), 2, RoundingMode.HALF_UP);
        }

        // Project future months starting from the month after the latest historical data (or current month)
        YearMonth latestHistoricalMonth = nonZeroMonths.get(nonZeroMonths.size() - 1).getKey();
        YearMonth currentMonth = YearMonth.from(LocalDate.now());
        YearMonth startProjectionFrom = latestHistoricalMonth.isAfter(currentMonth) ? latestHistoricalMonth : currentMonth;

        List<ForecastMonthDto> forecastList = new ArrayList<>();
        for (int i = 1; i <= months; i++) {
            YearMonth futureMonth = startProjectionFrom.plusMonths(i);
            forecastList.add(ForecastMonthDto.builder()
                    .month(futureMonth.format(MONTH_FORMATTER))
                    .projectedRevenue(projectedMonthlyRevenue)
                    .build());
        }

        String forecastPeriod = forecastList.get(0).getMonth() + " to " + forecastList.get(forecastList.size() - 1).getMonth();

        return RevenueForecastResponse.builder()
                .methodology(selectedMethodology)
                .confidence("BASELINE")
                .isSufficientData(true)
                .message("Deterministic baseline forecast generated successfully based on " + historicalCount + " historical months.")
                .historicalPeriodMonths(historicalCount)
                .averageMonthlyRevenue(projectedMonthlyRevenue)
                .historicalRevenue(historicalList)
                .forecast(forecastList)
                .forecastPeriod(forecastPeriod)
                .build();
    }
}
