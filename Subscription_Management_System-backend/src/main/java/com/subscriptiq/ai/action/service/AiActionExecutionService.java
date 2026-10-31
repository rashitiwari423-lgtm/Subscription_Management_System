package com.subscriptiq.ai.action.service;

import com.subscriptiq.ai.action.dto.ActionExecutionRequest;
import com.subscriptiq.ai.action.dto.ActionExecutionResponse;
import com.subscriptiq.security.UserPrincipal;

public interface AiActionExecutionService {

    ActionExecutionResponse executeAction(Long actionId, UserPrincipal user, ActionExecutionRequest request);
}

