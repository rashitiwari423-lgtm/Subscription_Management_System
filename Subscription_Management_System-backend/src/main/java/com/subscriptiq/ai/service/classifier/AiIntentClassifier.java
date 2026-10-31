package com.subscriptiq.ai.service.classifier;

import com.subscriptiq.ai.enums.AiIntent;

public interface AiIntentClassifier {

    AiIntent classifyIntent(String question);

    boolean isMaliciousOrInjection(String question);
}

