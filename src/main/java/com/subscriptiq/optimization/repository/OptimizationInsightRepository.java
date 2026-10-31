package com.subscriptiq.optimization.repository;

import com.subscriptiq.optimization.entity.OptimizationInsight;
import com.subscriptiq.optimization.enums.OptimizationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface OptimizationInsightRepository extends JpaRepository<OptimizationInsight, Long>, JpaSpecificationExecutor<OptimizationInsight> {

    boolean existsByUsageRecordId(Long usageRecordId);

    Optional<OptimizationInsight> findByUsageRecordId(Long usageRecordId);

    Page<OptimizationInsight> findByCustomerId(Long customerId, Pageable pageable);

    Page<OptimizationInsight> findBySubscriptionId(Long subscriptionId, Pageable pageable);

    Page<OptimizationInsight> findByStatusIn(Collection<OptimizationStatus> statuses, Pageable pageable);
}

