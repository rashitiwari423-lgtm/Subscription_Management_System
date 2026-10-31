package com.subscriptiq.ai.service;

import com.subscriptiq.ai.dto.AiInsightResponse;
import com.subscriptiq.ai.dto.UpdateAiInsightStatusRequest;
import com.subscriptiq.ai.entity.AiInsight;
import com.subscriptiq.ai.enums.AiInsightStatus;
import com.subscriptiq.ai.enums.AiSeverity;
import com.subscriptiq.ai.repository.AiInsightRepository;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.common.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiInsightServiceImpl implements AiInsightService {

    private final AiInsightRepository aiInsightRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AiInsightResponse> getInsights(AiSeverity severity, AiInsightStatus status, String type, Pageable pageable) {
        Specification<AiInsight> spec = Specification.where(null);

        if (severity != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("severity"), severity));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (type != null && !type.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type.trim()));
        }

        Page<AiInsightResponse> page = aiInsightRepository.findAll(spec, pageable).map(this::mapToResponse);
        return PagedResponse.fromPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public AiInsightResponse getInsightById(Long id) {
        AiInsight insight = aiInsightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI insight not found with id: " + id));
        return mapToResponse(insight);
    }

    @Override
    @Transactional
    public AiInsightResponse updateStatus(Long id, UpdateAiInsightStatusRequest request) {
        AiInsight insight = aiInsightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI insight not found with id: " + id));

        insight.setStatus(request.getStatus());
        AiInsight saved = aiInsightRepository.save(insight);
        log.info("Updated AI insight [{}] status to [{}]", saved.getId(), saved.getStatus());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AiInsight saveInsight(AiInsight insight) {
        return aiInsightRepository.save(insight);
    }

    public AiInsightResponse mapToResponse(AiInsight insight) {
        return AiInsightResponse.builder()
                .id(insight.getId())
                .type(insight.getType())
                .title(insight.getTitle())
                .description(insight.getDescription())
                .severity(insight.getSeverity())
                .affectedCustomers(insight.getAffectedCustomers())
                .affectedSubscriptions(insight.getAffectedSubscriptions())
                .affectedInvoices(insight.getAffectedInvoices())
                .potentialRevenue(insight.getPotentialRevenue())
                .potentialSavings(insight.getPotentialSavings())
                .recommendedAction(insight.getRecommendedAction())
                .status(insight.getStatus())
                .createdAt(insight.getCreatedAt())
                .updatedAt(insight.getUpdatedAt())
                .build();
    }
}

