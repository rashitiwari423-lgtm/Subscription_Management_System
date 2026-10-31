package com.subscriptiq.ai.action.service;

import com.subscriptiq.ai.action.dto.ActionApprovalRequest;
import com.subscriptiq.ai.action.dto.ActionExecutionResponse;
import com.subscriptiq.ai.action.dto.ActionRejectionRequest;
import com.subscriptiq.ai.action.dto.ActionResponse;
import com.subscriptiq.ai.action.enums.AiActionExecutionStatus;
import com.subscriptiq.ai.action.enums.AiActionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.security.UserPrincipal;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AiActionService {

    ActionResponse approveAction(Long actionId, UserPrincipal user, ActionApprovalRequest request);

    ActionResponse rejectAction(Long actionId, UserPrincipal user, ActionRejectionRequest request);

    ActionResponse getActionById(Long actionId);

    PagedResponse<ActionResponse> getActions(
            AiActionType type,
            AiActionStatus status,
            String requestedBy,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable);

    PagedResponse<ActionExecutionResponse> getExecutionHistory(
            AiActionType type,
            AiActionExecutionStatus status,
            String executedBy,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable);
}

