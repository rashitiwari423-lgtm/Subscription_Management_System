package com.subscriptiq.optimization.controller;

import com.subscriptiq.common.response.ApiResponse;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.optimization.dto.AnalyzeUsageRequest;
import com.subscriptiq.optimization.dto.OptimizationInsightResponse;
import com.subscriptiq.optimization.service.OptimizationService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/optimization")
@RequiredArgsConstructor
@Tag(name = "Cost Optimization", description = "Deterministic cost optimization analysis, license recommendations, savings calculation, preview and approval")
public class OptimizationController {

    private final OptimizationService optimizationService;

    @PostMapping("/analyze")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Analyze resource usage", description = "Triggers deterministic cost optimization analysis across usage records. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<List<OptimizationInsightResponse>>> analyze(
            @Valid @RequestBody(required = false) AnalyzeUsageRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<OptimizationInsightResponse> responses = optimizationService.analyze(request, principal);
        return ResponseEntity.ok(ApiResponse.success("Optimization analysis completed successfully", responses));
    }

    @GetMapping("/insights")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "List all optimization insights", description = "Paginated list of all optimization insights. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<PagedResponse<OptimizationInsightResponse>>> getInsights(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<OptimizationInsightResponse> response = optimizationService.getInsights(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/insights/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get optimization insight by ID", description = "Retrieves details of an insight. CUSTOMER can only view their own linked insight.")
    public ResponseEntity<ApiResponse<OptimizationInsightResponse>> getInsightById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        OptimizationInsightResponse response = optimizationService.getInsightById(id, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/customers/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "List customer optimization insights", description = "Lists insights for a specific customer. CUSTOMER can only view their own insights.")
    public ResponseEntity<ApiResponse<PagedResponse<OptimizationInsightResponse>>> getCustomerInsights(
            @PathVariable Long customerId,
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<OptimizationInsightResponse> response = optimizationService.getCustomerInsights(customerId, principal, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/subscriptions/{subscriptionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "List subscription optimization insights", description = "Lists insights for a specific subscription. CUSTOMER can only view their own insights.")
    public ResponseEntity<ApiResponse<PagedResponse<OptimizationInsightResponse>>> getSubscriptionInsights(
            @PathVariable Long subscriptionId,
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<OptimizationInsightResponse> response = optimizationService.getSubscriptionInsights(subscriptionId, principal, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/insights/{id}/preview")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Preview optimization insight", description = "Generates/transitions insight to PREVIEWED status with cost savings breakdown. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<OptimizationInsightResponse>> previewInsight(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        OptimizationInsightResponse response = optimizationService.previewInsight(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Optimization insight previewed successfully", response));
    }

    @PostMapping("/insights/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve optimization insight", description = "Approves the optimization recommendation without mutating billing or subscriptions directly. Restricted to ADMIN.")
    public ResponseEntity<ApiResponse<OptimizationInsightResponse>> approveInsight(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        OptimizationInsightResponse response = optimizationService.approveInsight(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Optimization insight approved successfully", response));
    }

    @PostMapping("/insights/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject optimization insight", description = "Rejects the optimization recommendation. Restricted to ADMIN.")
    public ResponseEntity<ApiResponse<OptimizationInsightResponse>> rejectInsight(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserPrincipal principal) {
        OptimizationInsightResponse response = optimizationService.rejectInsight(id, reason, principal);
        return ResponseEntity.ok(ApiResponse.success("Optimization insight rejected successfully", response));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "List optimization history", description = "Paginated list of approved, rejected, and executed insights. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<PagedResponse<OptimizationInsightResponse>>> getHistory(
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<OptimizationInsightResponse> response = optimizationService.getHistory(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

