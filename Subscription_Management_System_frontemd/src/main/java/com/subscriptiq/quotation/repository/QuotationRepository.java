package com.subscriptiq.quotation.repository;

import com.subscriptiq.quotation.entity.Quotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long>, JpaSpecificationExecutor<Quotation> {

    Optional<Quotation> findByQuotationNumber(String quotationNumber);

    boolean existsByQuotationNumber(String quotationNumber);

    @Query("SELECT q FROM Quotation q WHERE q.customer.id = :customerId")
    Page<Quotation> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    @Query("SELECT q FROM Quotation q WHERE q.customer.user.id = :userId")
    Page<Quotation> findByCustomerUserId(@Param("userId") Long userId, Pageable pageable);
}

