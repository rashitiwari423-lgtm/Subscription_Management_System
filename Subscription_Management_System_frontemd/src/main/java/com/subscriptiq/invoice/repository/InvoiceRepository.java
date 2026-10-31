package com.subscriptiq.invoice.repository;

import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    Page<Invoice> findByCustomerId(Long customerId, Pageable pageable);

    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByStatusIn(List<InvoiceStatus> statuses);

    Page<Invoice> findByCustomerIdAndStatus(Long customerId, InvoiceStatus status, Pageable pageable);

    List<Invoice> findByCustomerId(Long customerId);

    List<Invoice> findBySubscriptionId(Long subscriptionId);

    boolean existsBySubscriptionIdAndBillingPeriodStart(Long subscriptionId, java.time.LocalDate billingPeriodStart);

    boolean existsBySubscriptionIdAndBillingPeriodStartAndBillingPeriodEnd(Long subscriptionId, java.time.LocalDate billingPeriodStart, java.time.LocalDate billingPeriodEnd);

    Optional<Invoice> findBySubscriptionIdAndBillingPeriodStart(Long subscriptionId, java.time.LocalDate billingPeriodStart);

    @Query("SELECT i FROM Invoice i WHERE i.status IN (:statuses) AND i.dueDate < :currentDate AND i.balanceAmount > 0")
    List<Invoice> findOverdueCandidates(@Param("statuses") List<InvoiceStatus> statuses, @Param("currentDate") java.time.LocalDate currentDate);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.invoiceNumber LIKE CONCAT(:prefix, '%')")
    long countByInvoiceNumberPrefix(@Param("prefix") String prefix);
}

