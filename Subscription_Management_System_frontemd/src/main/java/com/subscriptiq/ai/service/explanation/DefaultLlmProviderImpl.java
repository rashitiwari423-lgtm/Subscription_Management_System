package com.subscriptiq.ai.service.explanation;

import com.subscriptiq.ai.enums.AiIntent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DefaultLlmProviderImpl implements LlmProvider {

    @Value("${subscriptiq.ai.llm.enabled:false}")
    private boolean enabled;

    @Value("${subscriptiq.ai.llm.api-key:}")
    private String apiKey;

    @Override
    public boolean isAvailable() {
        return enabled && apiKey != null && !apiKey.trim().isEmpty();
    }

    @Override
    public String generateExplanation(AiIntent intent, Object structuredData) {
        if (!isAvailable()) {
            log.debug("LLM provider unavailable or unconfigured, delegating to deterministic fallback");
            return null;
        }
        // When configured with external API, format prompt with structured facts only
        // The LLM produces natural language without DB access
        log.info("Generating LLM explanation for intent [{}] with structured facts", intent);
        return null;
    }
}

