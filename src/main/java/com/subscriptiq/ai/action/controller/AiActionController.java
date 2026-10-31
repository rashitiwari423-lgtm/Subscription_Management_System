package com.subscriptiq.ai.action.controller;

import com.subscriptiq.ai.action.dto.ActionApprovalRequest;
import com.subscriptiq.ai.action.dto.ActionExecutionRequest;
import com.subscriptiq.ai.action.dto.ActionExecutionResponse;
import com.subscriptiq.ai.action.dto.ActionPreviewRequest;
import com.subscriptiq.ai.action.dto.ActionPreviewResponse;
import com.subscriptiq.ai.action.dto.ActionRejectionRequest;
import com.subscriptiq.ai.action.dto.ActionResponse;
import com.subscriptiq.ai.action.enums.AiActionExecutionStatus;
import com.subscriptiq.ai.action.enums.AiActionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import com.subscriptiq.ai.action.service.AiActionExecutionService;
import com.subscriptiq.ai.action.service.AiActionPreviewService;
import com.subscriptiq.ai.action.service.AiActionService;
import com.subscriptiq.common.response.ApiResponse;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/ai/actions")
@RequiredArgsConstructor
@Tag(name = "AI Action Command Center", description = "Controlled AI business action preview, approval, and execution APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AiActionController {

    private final AiActionPreviewService previewService;
    private final AiActionService actionService;
    private final AiActionExecutionService executionService;

    @PostMapping("/preview")
    @Operation(summary = "Preview an AI recommended business action", description = "Calculates actual affected counts, revenue, and savings without executing any mutations.")
    public ResponseEntity<ApiResponse<ActionPreviewResponse>> previewAction(
            @Valid @RequestBody ActionPreviewRequest request,
            @AuthenticationPrincipal UserPrincipal user) {

        ActionPreviewResponse response = previewService.previewAction(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Action preview generated successfully", response));
    }

    @PostMapping("/{actionId}/approve")
    @Operation(summary = "Approve an AI action request", description = "Transitions action status from PENDING_APPROVAL to APPROVED. ADMIN only.")
    public ResponseEntity<ApiResponse<ActionResponse>> approveAction(
            @PathVariable Long actionId,
            @RequestBody(required = false) ActionApprovalRequest request,
            @AuthenticationPrincipal UserPrincipal user) {

        ActionResponse response = actionService.approveAction(actionId, user, request);
        return ResponseEntity.ok(ApiResponse.success("Action approved successfully", response));
    }

    @PostMapping("/{actionId}/reject")
    @Operation(summary = "Reject an AI action request", description = "Transitions action status to REJECTED. ADMIN only.")
    public ResponseEntity<ApiResponse<ActionResponse>> rejectAction(
            @PathVariable Long actionId,
            @RequestBody(required = false) ActionRejectionRequest request,
            @AuthenticationPrincipal UserPrincipal user) {

        ActionResponse response = actionService.rejectAction(actionId, user, request);
        return ResponseEntity.ok(ApiResponse.success("Action rejected successfully", response));
    }

    @PostMapping("/{actionId}/execute")
    @Operation(summary = "Execute an approved AI action", description = "Runs the dedicated server-side action executor. Enforces idempotency and server-side validation. ADMIN only.")
    public ResponseEntity<ApiResponse<ActionExecutionResponse>> executeAction(
            @PathVariable Long actionId,
            @RequestBody(required = false) ActionExecutionRequest request,
            @AuthenticationPrincipal UserPrincipal user) {

        ActionExecutionResponse response = executionService.executeAction(actionId, user, request);
        return ResponseEntity.ok(ApiResponse.success("Action executed successfully", response));
    }

    @GetMapping
    @Operation(summary = "List AI action requests", description = "Retrieves a paginated list of action requests with optional status, type, and date filters.")
    public ResponseEntity<ApiResponse<PagedResponse<ActionResponse>>> getActions(
            @RequestParam(required = false) AiActionType actionType,
            @RequestParam(required = false) AiActionStatus status,
            @RequestParam(required = false) String requestedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PagedResponse<ActionResponse> response = actionService.getActions(actionType, status, requestedBy, startDate, endDate, pageRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{actionId}")
    @Operation(summary = "Get action request details", description = "Retrieves full details of a specific action request.")
    public ResponseEntity<ApiResponse<ActionResponse>> getActionById(@PathVariable Long actionId) {
        ActionResponse response = actionService.getActionById(actionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/history")
    @Operation(summary = "Get action execution history", description = "Retrieves a paginated audit trail of executed AI actions.")
    public ResponseEntity<ApiResponse<PagedResponse<ActionExecutionResponse>>> getExecutionHistory(
            @RequestParam(required = false) AiActionType actionType,
            @RequestParam(required = false) AiActionExecutionStatus status,
            @RequestParam(required = false) String executedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "executedAt"));
        PagedResponse<ActionExecutionResponse> response = actionService.getExecutionHistory(actionType, status, executedBy, startDate, endDate, pageRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

