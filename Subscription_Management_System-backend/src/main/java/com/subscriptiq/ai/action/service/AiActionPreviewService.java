package com.subscriptiq.ai.action.service;

import com.subscriptiq.ai.action.dto.ActionPreviewRequest;
import com.subscriptiq.ai.action.dto.ActionPreviewResponse;
import com.subscriptiq.security.UserPrincipal;

public interface AiActionPreviewService {

    ActionPreviewResponse previewAction(ActionPreviewRequest request, UserPrincipal user);
}

