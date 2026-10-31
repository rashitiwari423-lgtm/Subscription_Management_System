package com.subscriptiq.ai.service;

import com.subscriptiq.ai.dto.AiInsightResponse;
import com.subscriptiq.ai.dto.UpdateAiInsightStatusRequest;
import com.subscriptiq.ai.entity.AiInsight;
import com.subscriptiq.ai.enums.AiInsightStatus;
import com.subscriptiq.ai.enums.AiSeverity;
import com.subscriptiq.common.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface AiInsightService {

    PagedResponse<AiInsightResponse> getInsights(AiSeverity severity, AiInsightStatus status, String type, Pageable pageable);

    AiInsightResponse getInsightById(Long id);

    AiInsightResponse updateStatus(Long id, UpdateAiInsightStatusRequest request);

    AiInsight saveInsight(AiInsight insight);
}

