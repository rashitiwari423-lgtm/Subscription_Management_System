package com.subscriptiq.usage.repository;

import com.subscriptiq.usage.entity.UsageRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long>, JpaSpecificationExecutor<UsageRecord> {

    Page<UsageRecord> findByCustomerId(Long customerId, Pageable pageable);

    Page<UsageRecord> findBySubscriptionId(Long subscriptionId, Pageable pageable);

    List<UsageRecord> findByCustomerIdAndSnapshotDate(Long customerId, LocalDate snapshotDate);

    List<UsageRecord> findBySubscriptionIdAndSnapshotDate(Long subscriptionId, LocalDate snapshotDate);
}

