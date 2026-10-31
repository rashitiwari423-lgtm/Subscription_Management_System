package com.subscriptiq.ai.action.repository;

import com.subscriptiq.ai.action.entity.AiActionAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiActionAuditLogRepository extends JpaRepository<AiActionAuditLog, Long>, JpaSpecificationExecutor<AiActionAuditLog> {

    List<AiActionAuditLog> findByActionId(Long actionId);

    List<AiActionAuditLog> findByInsightId(Long insightId);

    Page<AiActionAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

