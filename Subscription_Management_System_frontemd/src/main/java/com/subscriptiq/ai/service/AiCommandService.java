package com.subscriptiq.ai.service;

import com.subscriptiq.ai.dto.AiAskRequest;
import com.subscriptiq.ai.dto.AiAskResponse;
import com.subscriptiq.security.UserPrincipal;

public interface AiCommandService {

    AiAskResponse ask(AiAskRequest request, UserPrincipal principal);
}

