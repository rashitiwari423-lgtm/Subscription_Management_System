package com.subscriptiq.analytics.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface AnalyticsRepository {

    long countActiveCustomers();

    long countActiveSubscriptions();

    long countExpiringSubscriptions(LocalDate cutoffDate);

    BigDecimal sumTotalBilledAmount();

    BigDecimal sumTotalCollectedAmount();

    BigDecimal sumMonthlyRecurringRevenue();
}
