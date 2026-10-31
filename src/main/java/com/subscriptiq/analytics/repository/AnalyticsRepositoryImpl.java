package com.subscriptiq.analytics.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public class AnalyticsRepositoryImpl implements AnalyticsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public long countActiveCustomers() {
        return entityManager.createQuery(
                "SELECT COUNT(c) FROM Customer c WHERE c.status = com.subscriptiq.customer.enums.CustomerStatus.ACTIVE", Long.class)
                .getSingleResult();
    }

    @Override
    public long countActiveSubscriptions() {
        return entityManager.createQuery(
                "SELECT COUNT(s) FROM Subscription s WHERE s.status = com.subscriptiq.subscription.enums.SubscriptionStatus.ACTIVE", Long.class)
                .getSingleResult();
    }

    @Override
    public long countExpiringSubscriptions(LocalDate cutoffDate) {
        return entityManager.createQuery(
                "SELECT COUNT(s) FROM Subscription s WHERE s.status = com.subscriptiq.subscription.enums.SubscriptionStatus.ACTIVE AND s.endDate IS NOT NULL AND s.endDate <= :cutoffDate", Long.class)
                .setParameter("cutoffDate", cutoffDate)
                .getSingleResult();
    }

    @Override
    public BigDecimal sumTotalBilledAmount() {
        BigDecimal result = entityManager.createQuery(
                "SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i", BigDecimal.class)
                .getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal sumTotalCollectedAmount() {
        BigDecimal result = entityManager.createQuery(
                "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = com.subscriptiq.payment.enums.PaymentStatus.SUCCESS", BigDecimal.class)
                .getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal sumMonthlyRecurringRevenue() {
        BigDecimal result = entityManager.createQuery(
                "SELECT COALESCE(SUM(s.totalAmount), 0) FROM Subscription s WHERE s.status = com.subscriptiq.subscription.enums.SubscriptionStatus.ACTIVE", BigDecimal.class)
                .getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }
}
