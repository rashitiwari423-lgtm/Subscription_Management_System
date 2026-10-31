package com.subscriptiq.ai.action.validator;

import com.subscriptiq.ai.action.dto.ActionPreviewRequest;
import com.subscriptiq.ai.action.entity.AiActionRequest;
import com.subscriptiq.ai.action.enums.AiActionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import com.subscriptiq.ai.entity.AiInsight;
import com.subscriptiq.ai.enums.AiInsightStatus;
import com.subscriptiq.ai.enums.AiRecommendedAction;
import com.subscriptiq.ai.repository.AiInsightRepository;
import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiActionValidator {

    private final AiInsightRepository aiInsightRepository;

    public AiInsight validatePreviewRequest(ActionPreviewRequest request) {
        if (request == null || request.getInsightId() == null) {
            throw new BadRequestException("Insight ID is required for action preview");
        }
        if (request.getActionType() == null) {
            throw new BadRequestException("Action type is required for action preview");
        }

        AiInsight insight = aiInsightRepository.findById(request.getInsightId())
                .orElseThrow(() -> new ResourceNotFoundException("AI Insight not found with id: " + request.getInsightId()));

        if (insight.getStatus() == AiInsightStatus.RESOLVED || insight.getStatus() == AiInsightStatus.DISMISSED) {
            throw new BadRequestException(String.format("AI Insight [%d] is %s and cannot be acted upon",
                    insight.getId(), insight.getStatus()));
        }

        AiRecommendedAction recommendedAction = insight.getRecommendedAction();
        if (recommendedAction == null || recommendedAction == AiRecommendedAction.NO_ACTION) {
            throw new BadRequestException(String.format("AI Insight [%d] does not contain an actionable recommendation", insight.getId()));
        }

        // Validate that requested action type strictly matches the insight recommendation
        if (!isActionCompatible(request.getActionType(), recommendedAction)) {
            throw new BadRequestException(String.format("Requested action type [%s] does not match insight recommendation [%s]",
                    request.getActionType(), recommendedAction));
        }

        return insight;
    }

    public void validateApprovalRequest(AiActionRequest action) {
        if (action == null) {
            throw new ResourceNotFoundException("Action request not found");
        }

        if (action.getStatus() != AiActionStatus.PENDING_APPROVAL) {
            throw new BadRequestException(String.format("Action [%d] is not pending approval. Current status is %s",
                    action.getId(), action.getStatus()));
        }
    }

    public void validateRejectionRequest(AiActionRequest action) {
        if (action == null) {
            throw new ResourceNotFoundException("Action request not found");
        }

        if (action.getStatus() != AiActionStatus.PENDING_APPROVAL) {
            throw new BadRequestException(String.format("Action [%d] cannot be rejected. Current status is %s",
                    action.getId(), action.getStatus()));
        }
    }

    public void validateExecutionRequest(AiActionRequest action) {
        if (action == null) {
            throw new ResourceNotFoundException("Action request not found");
        }

        if (action.getStatus() == AiActionStatus.REJECTED) {
            throw new BadRequestException(String.format("Cannot execute action [%d] because it has been REJECTED", action.getId()));
        }

        if (action.getStatus() == AiActionStatus.CANCELLED) {
            throw new BadRequestException(String.format("Cannot execute action [%d] because it has been CANCELLED", action.getId()));
        }

        if (action.getStatus() == AiActionStatus.PENDING_APPROVAL || action.getStatus() == AiActionStatus.REQUESTED) {
            throw new BadRequestException(String.format("Action [%d] must be approved by an administrator before execution", action.getId()));
        }

        // Note: If action.getStatus() == EXECUTED, execution service will handle idempotency
        if (action.getStatus() != AiActionStatus.APPROVED && action.getStatus() != AiActionStatus.EXECUTED) {
            throw new BadRequestException(String.format("Action [%d] cannot be executed in status %s",
                    action.getId(), action.getStatus()));
        }
    }

    private boolean isActionCompatible(AiActionType actionType, AiRecommendedAction recommendation) {
        return actionType.name().equals(recommendation.name());
    }
}

