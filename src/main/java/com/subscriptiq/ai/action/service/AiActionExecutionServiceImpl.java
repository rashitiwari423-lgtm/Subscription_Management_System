package com.subscriptiq.ai.action.service;

import com.subscriptiq.ai.action.dto.ActionExecutionRequest;
import com.subscriptiq.ai.action.dto.ActionExecutionResponse;
import com.subscriptiq.ai.action.dto.ActionExecutionResult;
import com.subscriptiq.ai.action.entity.AiActionAuditLog;
import com.subscriptiq.ai.action.entity.AiActionExecution;
import com.subscriptiq.ai.action.entity.AiActionRequest;
import com.subscriptiq.ai.action.enums.AiActionExecutionStatus;
import com.subscriptiq.ai.action.enums.AiActionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import com.subscriptiq.ai.action.executor.ActionExecutor;
import com.subscriptiq.ai.action.repository.AiActionAuditLogRepository;
import com.subscriptiq.ai.action.repository.AiActionExecutionRepository;
import com.subscriptiq.ai.action.repository.AiActionRequestRepository;
import com.subscriptiq.ai.action.validator.AiActionValidator;
import com.subscriptiq.ai.entity.AiInsight;
import com.subscriptiq.ai.enums.AiInsightStatus;
import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiActionExecutionServiceImpl implements AiActionExecutionService {

    private final AiActionRequestRepository actionRequestRepository;
    private final AiActionExecutionRepository executionRepository;
    private final AiActionAuditLogRepository auditLogRepository;
    private final AiActionValidator validator;
    private final Map<AiActionType, ActionExecutor> executorMap;

    public AiActionExecutionServiceImpl(
            AiActionRequestRepository actionRequestRepository,
            AiActionExecutionRepository executionRepository,
            AiActionAuditLogRepository auditLogRepository,
            AiActionValidator validator,
            List<ActionExecutor> executors) {
        this.actionRequestRepository = actionRequestRepository;
        this.executionRepository = executionRepository;
        this.auditLogRepository = auditLogRepository;
        this.validator = validator;
        this.executorMap = executors.stream()
                .collect(Collectors.toMap(ActionExecutor::getActionType, Function.identity()));
    }

    @Override
    @Transactional
    public ActionExecutionResponse executeAction(Long actionId, UserPrincipal user, ActionExecutionRequest request) {
        AiActionRequest action = actionRequestRepository.findById(actionId)
                .orElseThrow(() -> new ResourceNotFoundException("Action not found with id: " + actionId));

        String actorEmail = user != null ? user.getEmail() : "admin@system.local";

        // Idempotency check 1: If action is already EXECUTED, return previous execution result
        if (action.getStatus() == AiActionStatus.EXECUTED) {
            log.info("Action [{}] has already been EXECUTED. Returning previous execution result (Idempotent call).", actionId);
            Optional<AiActionExecution> prevExecOpt = executionRepository.findFirstByActionIdOrderByExecutedAtDesc(action.getId());
            if (prevExecOpt.isPresent()) {
                AiActionExecution prev = prevExecOpt.get();
                return mapToResponse(prev, true);
            }
        }

        // Idempotency check 2: Check by request idempotencyKey if provided
        if (request != null && StringUtils.hasText(request.getIdempotencyKey())) {
            Optional<AiActionRequest> existingWithKey = actionRequestRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingWithKey.isPresent() && existingWithKey.get().getStatus() == AiActionStatus.EXECUTED) {
                log.info("Action with idempotencyKey [{}] already executed. Returning previous result.", request.getIdempotencyKey());
                Optional<AiActionExecution> prevExecOpt = executionRepository.findFirstByActionIdOrderByExecutedAtDesc(existingWithKey.get().getId());
                if (prevExecOpt.isPresent()) {
                    return mapToResponse(prevExecOpt.get(), true);
                }
            }
        }

        // Validate that action is in APPROVED status
        validator.validateExecutionRequest(action);

        ActionExecutor executor = executorMap.get(action.getActionType());
        if (executor == null) {
            throw new BadRequestException("No executor registered for action type: " + action.getActionType());
        }

        // Set status to EXECUTING
        action.setStatus(AiActionStatus.EXECUTING);
        actionRequestRepository.save(action);

        try {
            // Server-side dispatch to the whitelisted executor
            ActionExecutionResult result = executor.execute(action, user);

            if (result.getStatus() == AiActionExecutionStatus.SUCCESS) {
                action.setStatus(AiActionStatus.EXECUTED);

                // Transition associated insight to RESOLVED
                AiInsight insight = action.getInsight();
                if (insight != null) {
                    insight.setStatus(AiInsightStatus.RESOLVED);
                }
                actionRequestRepository.save(action);

                // Create execution record
                AiActionExecution execution = AiActionExecution.builder()
                        .action(action)
                        .actionType(action.getActionType())
                        .executedBy(actorEmail)
                        .status(AiActionExecutionStatus.SUCCESS)
                        .affectedCount(result.getAffectedCount())
                        .resultSummary(result.getSummary())
                        .executedAt(LocalDateTime.now())
                        .build();
                AiActionExecution savedExec = executionRepository.save(execution);

                // Create audit log
                AiActionAuditLog auditLog = AiActionAuditLog.builder()
                        .actor(actorEmail)
                        .action("EXECUTE_ACTION")
                        .actionType(action.getActionType())
                        .actionId(action.getId())
                        .insightId(insight != null ? insight.getId() : null)
                        .result("SUCCESS")
                        .affectedCount(result.getAffectedCount())
                        .details(result.getSummary())
                        .build();
                auditLogRepository.save(auditLog);

                log.info("Action [{}] executed successfully by [{}]: {}", actionId, actorEmail, result.getSummary());
                return mapToResponse(savedExec, false);
            } else {
                action.setStatus(AiActionStatus.FAILED);
                actionRequestRepository.save(action);

                AiActionExecution execution = AiActionExecution.builder()
                        .action(action)
                        .actionType(action.getActionType())
                        .executedBy(actorEmail)
                        .status(AiActionExecutionStatus.FAILED)
                        .affectedCount(0)
                        .resultSummary(result.getSummary())
                        .errorCode(result.getErrorCode())
                        .executedAt(LocalDateTime.now())
                        .build();
                AiActionExecution savedExec = executionRepository.save(execution);

                AiActionAuditLog auditLog = AiActionAuditLog.builder()
                        .actor(actorEmail)
                        .action("EXECUTE_ACTION")
                        .actionType(action.getActionType())
                        .actionId(action.getId())
                        .insightId(action.getInsight() != null ? action.getInsight().getId() : null)
                        .result("FAILED")
                        .affectedCount(0)
                        .details(result.getSummary())
                        .build();
                auditLogRepository.save(auditLog);

                log.warn("Action [{}] execution reported failure: {}", actionId, result.getSummary());
                return mapToResponse(savedExec, false);
            }
        } catch (Exception e) {
            log.error("Action [{}] execution encountered unexpected exception: {}", actionId, e.getMessage(), e);
            action.setStatus(AiActionStatus.FAILED);
            actionRequestRepository.save(action);

            AiActionExecution execution = AiActionExecution.builder()
                    .action(action)
                    .actionType(action.getActionType())
                    .executedBy(actorEmail)
                    .status(AiActionExecutionStatus.FAILED)
                    .affectedCount(0)
                    .resultSummary("Execution failed: " + e.getMessage())
                    .errorCode("INTERNAL_EXECUTION_ERROR")
                    .executedAt(LocalDateTime.now())
                    .build();
            AiActionExecution savedExec = executionRepository.save(execution);

            AiActionAuditLog auditLog = AiActionAuditLog.builder()
                    .actor(actorEmail)
                    .action("EXECUTE_ACTION")
                    .actionType(action.getActionType())
                    .actionId(action.getId())
                    .insightId(action.getInsight() != null ? action.getInsight().getId() : null)
                    .result("FAILED")
                    .affectedCount(0)
                    .details("Unexpected failure: " + e.getMessage())
                    .build();
            auditLogRepository.save(auditLog);

            return mapToResponse(savedExec, false);
        }
    }

    private ActionExecutionResponse mapToResponse(AiActionExecution exec, boolean previouslyExecuted) {
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
                .previouslyExecuted(previouslyExecuted)
                .build();
    }
}

