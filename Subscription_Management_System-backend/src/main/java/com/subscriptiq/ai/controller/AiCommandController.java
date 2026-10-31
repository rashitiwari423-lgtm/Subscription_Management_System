package com.subscriptiq.ai.controller;

import com.subscriptiq.ai.dto.AiAskRequest;
import com.subscriptiq.ai.dto.AiAskResponse;
import com.subscriptiq.ai.dto.AiInsightResponse;
import com.subscriptiq.ai.dto.UpdateAiInsightStatusRequest;
import com.subscriptiq.ai.enums.AiInsightStatus;
import com.subscriptiq.ai.enums.AiSeverity;
import com.subscriptiq.ai.service.AiCommandService;
import com.subscriptiq.ai.service.AiInsightService;
import com.subscriptiq.common.response.ApiResponse;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Subscription Command Center", description = "Natural-language query processing, intent detection, deterministic analytics, and structured insights for administrators")
public class AiCommandController {

    private final AiCommandService aiCommandService;
    private final AiInsightService aiInsightService;

    @PostMapping("/ask")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ask natural-language business question",
            description = "Analyzes business query, classifies controlled intent, retrieves deterministic facts via backend tools, and generates structured insight. Restricted to ADMIN.")
    public ResponseEntity<ApiResponse<AiAskResponse>> ask(
            @Valid @RequestBody AiAskRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        AiAskResponse response = aiCommandService.ask(request, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/insights")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List AI insights",
            description = "Paginated list of detected business insights with optional filters for severity, status, and category. Restricted to ADMIN.")
    public ResponseEntity<ApiResponse<PagedResponse<AiInsightResponse>>> getInsights(
            @RequestParam(required = false) AiSeverity severity,
            @RequestParam(required = false) AiInsightStatus status,
            @RequestParam(required = false) String type,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<AiInsightResponse> response = aiInsightService.getInsights(severity, status, type, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/insights/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get AI insight by ID",
            description = "Retrieves details of a specific AI insight. Restricted to ADMIN.")
    public ResponseEntity<ApiResponse<AiInsightResponse>> getInsightById(@PathVariable Long id) {
        AiInsightResponse response = aiInsightService.getInsightById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/insights/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update AI insight status",
            description = "Updates the lifecycle status of an AI insight (e.g. NEW -> VIEWED -> ACTION_PENDING -> RESOLVED). Restricted to ADMIN.")
    public ResponseEntity<ApiResponse<AiInsightResponse>> updateInsightStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAiInsightStatusRequest request) {
        AiInsightResponse response = aiInsightService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Insight status updated successfully", response));
    }
}

