package com.subscriptiq.ai.action.service;

import com.subscriptiq.ai.action.dto.ActionPreviewDto;
import com.subscriptiq.ai.action.dto.ActionPreviewRequest;
import com.subscriptiq.ai.action.dto.ActionPreviewResponse;
import com.subscriptiq.ai.action.entity.AiActionAuditLog;
import com.subscriptiq.ai.action.entity.AiActionRequest;
import com.subscriptiq.ai.action.enums.AiActionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import com.subscriptiq.ai.action.executor.ActionExecutor;
import com.subscriptiq.ai.action.repository.AiActionAuditLogRepository;
import com.subscriptiq.ai.action.repository.AiActionRequestRepository;
import com.subscriptiq.ai.action.validator.AiActionValidator;
import com.subscriptiq.ai.entity.AiInsight;
import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiActionPreviewServiceImpl implements AiActionPreviewService {

    private final AiActionValidator validator;
    private final AiActionRequestRepository actionRequestRepository;
    private final AiActionAuditLogRepository auditLogRepository;
    private final Map<AiActionType, ActionExecutor> executorMap;

    public AiActionPreviewServiceImpl(
            AiActionValidator validator,
            AiActionRequestRepository actionRequestRepository,
            AiActionAuditLogRepository auditLogRepository,
            List<ActionExecutor> executors) {
        this.validator = validator;
        this.actionRequestRepository = actionRequestRepository;
        this.auditLogRepository = auditLogRepository;
        this.executorMap = executors.stream()
                .collect(Collectors.toMap(ActionExecutor::getActionType, Function.identity()));
    }

    @Override
    @Transactional
    public ActionPreviewResponse previewAction(ActionPreviewRequest request, UserPrincipal user) {
        AiInsight insight = validator.validatePreviewRequest(request);

        ActionExecutor executor = executorMap.get(request.getActionType());
        if (executor == null) {
            throw new BadRequestException("No executor registered for action type: " + request.getActionType());
        }

        // Calculate real-time preview data from the live database
        ActionPreviewDto previewData = executor.preview(insight);

        String idempotencyKey = StringUtils.hasText(request.getIdempotencyKey())
                ? request.getIdempotencyKey()
                : UUID.randomUUID().toString();

        String actorEmail = user != null ? user.getEmail() : "admin@system.local";

        String resourcesSummary = previewData.getAffectedResources() != null && !previewData.getAffectedResources().isEmpty()
                ? String.join("\n", previewData.getAffectedResources())
                : "";

        String warningsSummary = previewData.getWarnings() != null && !previewData.getWarnings().isEmpty()
                ? String.join("\n", previewData.getWarnings())
                : "";

        AiActionRequest actionRequest = AiActionRequest.builder()
                .insight(insight)
                .actionType(request.getActionType())
                .status(AiActionStatus.PENDING_APPROVAL)
                .title(previewData.getTitle())
                .description(previewData.getDescription())
                .affectedCustomers(previewData.getAffectedCustomers())
                .affectedSubscriptions(previewData.getAffectedSubscriptions())
                .affectedInvoices(previewData.getAffectedInvoices())
                .potentialRevenue(previewData.getPotentialRevenue())
                .potentialSavings(previewData.getPotentialSavings())
                .affectedResourcesSummary(resourcesSummary)
                .warnings(warningsSummary)
                .requiresApproval(true)
                .idempotencyKey(idempotencyKey)
                .requestedBy(actorEmail)
                .build();

        AiActionRequest saved = actionRequestRepository.save(actionRequest);

        // Record audit trail
        AiActionAuditLog auditLog = AiActionAuditLog.builder()
                .actor(actorEmail)
                .action("PREVIEW_ACTION")
                .actionType(request.getActionType())
                .actionId(saved.getId())
                .insightId(insight.getId())
                .result("PREVIEWED")
                .affectedCount(saved.getAffectedCustomers())
                .details(String.format("Action preview generated with %d affected customers and %d warnings",
                        saved.getAffectedCustomers(), previewData.getWarnings().size()))
                .build();
        auditLogRepository.save(auditLog);

        log.info("Action preview generated: actionId [{}], type [{}], requestedBy [{}]",
                saved.getId(), saved.getActionType(), actorEmail);

        return ActionPreviewResponse.builder()
                .actionId(saved.getId())
                .insightId(insight.getId())
                .actionType(saved.getActionType())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .affectedCustomers(saved.getAffectedCustomers())
                .affectedSubscriptions(saved.getAffectedSubscriptions())
                .affectedInvoices(saved.getAffectedInvoices())
                .potentialRevenue(saved.getPotentialRevenue())
                .potentialSavings(saved.getPotentialSavings())
                .affectedResources(previewData.getAffectedResources())
                .warnings(previewData.getWarnings())
                .requiresApproval(saved.getRequiresApproval())
                .status(saved.getStatus())
                .idempotencyKey(saved.getIdempotencyKey())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}

