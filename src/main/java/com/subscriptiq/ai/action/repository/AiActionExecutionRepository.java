package com.subscriptiq.ai.action.repository;

import com.subscriptiq.ai.action.entity.AiActionExecution;
import com.subscriptiq.ai.action.enums.AiActionExecutionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiActionExecutionRepository extends JpaRepository<AiActionExecution, Long>, JpaSpecificationExecutor<AiActionExecution> {

    List<AiActionExecution> findByActionId(Long actionId);

    Optional<AiActionExecution> findFirstByActionIdOrderByExecutedAtDesc(Long actionId);

    List<AiActionExecution> findByActionType(AiActionType actionType);

    List<AiActionExecution> findByStatus(AiActionExecutionStatus status);
}

