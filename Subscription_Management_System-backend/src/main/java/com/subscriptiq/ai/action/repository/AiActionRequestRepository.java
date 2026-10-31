package com.subscriptiq.ai.action.repository;

import com.subscriptiq.ai.action.entity.AiActionRequest;
import com.subscriptiq.ai.action.enums.AiActionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiActionRequestRepository extends JpaRepository<AiActionRequest, Long>, JpaSpecificationExecutor<AiActionRequest> {

    List<AiActionRequest> findByInsightId(Long insightId);

    Optional<AiActionRequest> findByIdempotencyKey(String idempotencyKey);

    List<AiActionStatus> findByStatus(AiActionStatus status);

    List<AiActionType> findByActionType(AiActionType actionType);

    boolean existsByInsightIdAndStatus(Long insightId, AiActionStatus status);
}

