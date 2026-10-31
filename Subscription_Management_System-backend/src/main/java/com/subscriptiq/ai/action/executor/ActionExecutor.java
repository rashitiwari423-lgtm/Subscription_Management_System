package com.subscriptiq.ai.action.executor;

import com.subscriptiq.ai.action.dto.ActionExecutionResult;
import com.subscriptiq.ai.action.dto.ActionPreviewDto;
import com.subscriptiq.ai.action.entity.AiActionRequest;
import com.subscriptiq.ai.action.enums.AiActionType;
import com.subscriptiq.ai.entity.AiInsight;
import com.subscriptiq.security.UserPrincipal;

public interface ActionExecutor {

    AiActionType getActionType();

    ActionPreviewDto preview(AiInsight insight);

    ActionExecutionResult execute(AiActionRequest action, UserPrincipal user);
}

