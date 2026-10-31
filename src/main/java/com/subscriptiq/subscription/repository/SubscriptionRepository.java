package com.subscriptiq.subscription.repository;

import com.subscriptiq.subscription.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {

    Optional<Subscription> findBySubscriptionNumber(String subscriptionNumber);

    boolean existsBySubscriptionNumber(String subscriptionNumber);

    @Query("SELECT s FROM Subscription s WHERE s.customer.id = :customerId")
    Page<Subscription> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    @Query("SELECT s FROM Subscription s WHERE s.customer.user.id = :userId")
    Page<Subscription> findByCustomerUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT s FROM Subscription s WHERE s.quotation.id = :quotationId")
    List<Subscription> findByQuotationId(@Param("quotationId") Long quotationId);

    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND s.nextBillingDate <= :currentDate ORDER BY s.id ASC")
    List<Subscription> findEligibleForBilling(@Param("status") com.subscriptiq.subscription.enums.SubscriptionStatus status, @Param("currentDate") java.time.LocalDate currentDate);

    List<Subscription> findByStatus(com.subscriptiq.subscription.enums.SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND s.endDate IS NOT NULL AND s.endDate <= :cutoffDate")
    List<Subscription> findExpiringSubscriptions(@Param("status") com.subscriptiq.subscription.enums.SubscriptionStatus status, @Param("cutoffDate") java.time.LocalDate cutoffDate);
}

