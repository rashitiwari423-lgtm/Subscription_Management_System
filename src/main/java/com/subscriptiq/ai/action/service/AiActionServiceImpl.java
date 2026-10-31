package com.subscriptiq.ai.action.service;

import com.subscriptiq.ai.action.dto.ActionApprovalRequest;
import com.subscriptiq.ai.action.dto.ActionExecutionResponse;
import com.subscriptiq.ai.action.dto.ActionRejectionRequest;
import com.subscriptiq.ai.action.dto.ActionResponse;
import com.subscriptiq.ai.action.entity.AiActionAuditLog;
import com.subscriptiq.ai.action.entity.AiActionExecution;
import com.subscriptiq.ai.action.entity.AiActionRequest;
import com.subscriptiq.ai.action.enums.AiActionExecutionStatus;
import com.subscriptiq.ai.action.enums.AiActionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import com.subscriptiq.ai.action.repository.AiActionAuditLogRepository;
import com.subscriptiq.ai.action.repository.AiActionExecutionRepository;
import com.subscriptiq.ai.action.repository.AiActionRequestRepository;
import com.subscriptiq.ai.action.validator.AiActionValidator;
import com.subscriptiq.ai.enums.AiInsightStatus;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.security.UserPrincipal;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiActionServiceImpl implements AiActionService {

    private final AiActionRequestRepository actionRequestRepository;
    private final AiActionExecutionRepository executionRepository;
    private final AiActionAuditLogRepository auditLogRepository;
    private final AiActionValidator validator;

    @Override
    @Transactional
    public ActionResponse approveAction(Long actionId, UserPrincipal user, ActionApprovalRequest request) {
        AiActionRequest action = actionRequestRepository.findById(actionId)
                .orElseThrow(() -> new ResourceNotFoundException("Action not found with id: " + actionId));

        validator.validateApprovalRequest(action);

        String approverEmail = user != null ? user.getEmail() : "admin@system.local";
        action.setStatus(AiActionStatus.APPROVED);
        action.setApprovedBy(approverEmail);
        action.setApprovedAt(LocalDateTime.now());

        if (action.getInsight() != null) {
            action.getInsight().setStatus(AiInsightStatus.ACTION_PENDING);
        }

        AiActionRequest saved = actionRequestRepository.save(action);

        // Record approval audit log
        String note = (request != null && StringUtils.hasText(request.getNote()))
                ? request.getNote()
                : "Action approved by administrator";

        AiActionAuditLog auditLog = AiActionAuditLog.builder()
                .actor(approverEmail)
                .action("APPROVE_ACTION")
                .actionType(saved.getActionType())
                .actionId(saved.getId())
                .insightId(saved.getInsight() != null ? saved.getInsight().getId() : null)
                .result("APPROVED")
                .affectedCount(saved.getAffectedCustomers())
                .details(note)
                .build();
        auditLogRepository.save(auditLog);

        log.info("Action [{}] approved by [{}]", actionId, approverEmail);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ActionResponse rejectAction(Long actionId, UserPrincipal user, ActionRejectionRequest request) {
        AiActionRequest action = actionRequestRepository.findById(actionId)
                .orElseThrow(() -> new ResourceNotFoundException("Action not found with id: " + actionId));

        validator.validateRejectionRequest(action);

        String rejecterEmail = user != null ? user.getEmail() : "admin@system.local";
        String reason = (request != null && StringUtils.hasText(request.getReason()))
                ? request.getReason()
                : "Action rejected by administrator";

        action.setStatus(AiActionStatus.REJECTED);
        action.setRejectedBy(rejecterEmail);
        action.setRejectedAt(LocalDateTime.now());
        action.setRejectionReason(reason);

        AiActionRequest saved = actionRequestRepository.save(action);

        // Record rejection audit log
        AiActionAuditLog auditLog = AiActionAuditLog.builder()
                .actor(rejecterEmail)
                .action("REJECT_ACTION")
                .actionType(saved.getActionType())
                .actionId(saved.getId())
                .insightId(saved.getInsight() != null ? saved.getInsight().getId() : null)
                .result("REJECTED")
                .affectedCount(saved.getAffectedCustomers())
                .details(reason)
                .build();
        auditLogRepository.save(auditLog);

        log.info("Action [{}] rejected by [{}] with reason: {}", actionId, rejecterEmail, reason);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ActionResponse getActionById(Long actionId) {
        AiActionRequest action = actionRequestRepository.findById(actionId)
                .orElseThrow(() -> new ResourceNotFoundException("Action not found with id: " + actionId));
        return mapToResponse(action);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ActionResponse> getActions(
            AiActionType type,
            AiActionStatus status,
            String requestedBy,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        Specification<AiActionRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (type != null) {
                predicates.add(cb.equal(root.get("actionType"), type));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(requestedBy)) {
                predicates.add(cb.like(cb.lower(root.get("requestedBy")), "%" + requestedBy.toLowerCase().trim() + "%"));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(LocalTime.MAX)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AiActionRequest> page = actionRequestRepository.findAll(spec, pageable);
        List<ActionResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PagedResponse.<ActionResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ActionExecutionResponse> getExecutionHistory(
            AiActionType type,
            AiActionExecutionStatus status,
            String executedBy,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        Specification<AiActionExecution> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (type != null) {
                predicates.add(cb.equal(root.get("actionType"), type));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(executedBy)) {
                predicates.add(cb.like(cb.lower(root.get("executedBy")), "%" + executedBy.toLowerCase().trim() + "%"));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("executedAt"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("executedAt"), endDate.atTime(LocalTime.MAX)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AiActionExecution> page = executionRepository.findAll(spec, pageable);
        List<ActionExecutionResponse> content = page.getContent().stream()
                .map(this::mapToExecutionResponse)
                .toList();

        return PagedResponse.<ActionExecutionResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private ActionResponse mapToResponse(AiActionRequest request) {
        List<String> resources = StringUtils.hasText(request.getAffectedResourcesSummary())
                ? Arrays.asList(request.getAffectedResourcesSummary().split("\n"))
                : Collections.emptyList();

        List<String> warnings = StringUtils.hasText(request.getWarnings())
                ? Arrays.asList(request.getWarnings().split("\n"))
                : Collections.emptyList();

        return ActionResponse.builder()
                .id(request.getId())
                .insightId(request.getInsight() != null ? request.getInsight().getId() : null)
                .actionType(request.getActionType())
                .status(request.getStatus())
                .title(request.getTitle())
                .description(request.getDescription())
                .affectedCustomers(request.getAffectedCustomers())
                .affectedSubscriptions(request.getAffectedSubscriptions())
                .affectedInvoices(request.getAffectedInvoices())
                .potentialRevenue(request.getPotentialRevenue())
                .potentialSavings(request.getPotentialSavings())
                .affectedResources(resources)
                .warnings(warnings)
                .requiresApproval(request.getRequiresApproval())
                .idempotencyKey(request.getIdempotencyKey())
                .requestedBy(request.getRequestedBy())
                .approvedBy(request.getApprovedBy())
                .approvedAt(request.getApprovedAt())
                .rejectedBy(request.getRejectedBy())
                .rejectedAt(request.getRejectedAt())
                .rejectionReason(request.getRejectionReason())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

    private ActionExecutionResponse mapToExecutionResponse(AiActionExecution exec) {
        return ActionExecutionResponse.builder()
                .executionId(exec.getId())
                .actionId(exec.getAction() != null ? exec.getAction().getId() : null)
                .insightId(exec.getAction() != null && exec.getAction().getInsight() != null
                        ? exec.getAction().getInsight().getId()
                        : null)
                .actionType(exec.getActionType())
                .status(exec.getStatus())
                .affectedCount(exec.getAffectedCount())
                .resultSummary(exec.getResultSummary())
                .errorCode(exec.getErrorCode())
                .executedBy(exec.getExecutedBy())
                .executedAt(exec.getExecutedAt())
                .previouslyExecuted(false)
                .build();
    }
}

