package com.subscriptiq.ai.repository;

import com.subscriptiq.ai.entity.AiInsight;
import com.subscriptiq.ai.enums.AiInsightStatus;
import com.subscriptiq.ai.enums.AiSeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiInsightRepository extends JpaRepository<AiInsight, Long>, JpaSpecificationExecutor<AiInsight> {

    Page<AiInsight> findBySeverity(AiSeverity severity, Pageable pageable);

    Page<AiInsight> findByStatus(AiInsightStatus status, Pageable pageable);

    Page<AiInsight> findByType(String type, Pageable pageable);

    Page<AiInsight> findBySeverityAndStatus(AiSeverity severity, AiInsightStatus status, Pageable pageable);

    List<AiInsight> findByStatusIn(List<AiInsightStatus> statuses);
}

