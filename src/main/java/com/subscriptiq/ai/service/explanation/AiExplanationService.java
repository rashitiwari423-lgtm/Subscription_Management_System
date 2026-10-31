package com.subscriptiq.ai.service.explanation;

import com.subscriptiq.ai.enums.AiIntent;

public interface AiExplanationService {

    String explain(AiIntent intent, Object structuredData);
}

