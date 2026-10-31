package com.subscriptiq.ai.service.explanation;

import com.subscriptiq.ai.enums.AiIntent;

public interface LlmProvider {

    boolean isAvailable();

    String generateExplanation(AiIntent intent, Object structuredData);
}

