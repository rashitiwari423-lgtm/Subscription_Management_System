package com.subscriptiq.payment.repository;

import com.subscriptiq.payment.entity.Payment;
import com.subscriptiq.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentNumber(String paymentNumber);

    boolean existsByPaymentNumber(String paymentNumber);

    Page<Payment> findByInvoiceId(Long invoiceId, Pageable pageable);

    List<Payment> findByInvoiceId(Long invoiceId);

    Page<Payment> findByCustomerId(Long customerId, Pageable pageable);

    List<Payment> findByCustomerId(Long customerId);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentNumber LIKE CONCAT(:prefix, '%')")
    long countByPaymentNumberPrefix(@Param("prefix") String prefix);
}

