package com.subscriptiq.plan.controller;

import com.subscriptiq.common.response.ApiResponse;
import com.subscriptiq.plan.dto.CreatePlanRequest;
import com.subscriptiq.plan.dto.PlanResponse;
import com.subscriptiq.plan.dto.UpdatePlanRequest;
import com.subscriptiq.plan.dto.UpdatePlanStatusRequest;
import com.subscriptiq.plan.service.PlanService;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.user.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Subscription Plans", description = "Subscription plan pricing and billing cycle management")
public class PlanController {

    private final PlanService planService;

    @PostMapping("/api/variants/{variantId}/plans")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Create plan", description = "Creates a new subscription plan under specified variant. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<PlanResponse>> createPlan(
            @PathVariable Long variantId,
            @Valid @RequestBody CreatePlanRequest request) {
        PlanResponse response = planService.createPlan(variantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Plan created successfully", response));
    }

    @GetMapping("/api/plans")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all plans", description = "Paginated list of subscription plans. Customers only see ACTIVE plans.")
    public ResponseEntity<ApiResponse<com.subscriptiq.common.response.PagedResponse<PlanResponse>>> getAllPlans(
            @org.springframework.web.bind.annotation.RequestParam(required = false) com.subscriptiq.plan.enums.PlanStatus status,
            @org.springframework.data.web.PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {
        com.subscriptiq.plan.enums.PlanStatus effectiveStatus = status;
        if (principal.getRole() == UserRole.CUSTOMER) {
            effectiveStatus = com.subscriptiq.plan.enums.PlanStatus.ACTIVE;
        }
        com.subscriptiq.common.response.PagedResponse<PlanResponse> response = planService.getAllPlans(effectiveStatus, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/variants/{variantId}/plans")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List plans for variant", description = "Retrieves all plans under a variant. Customers only see ACTIVE plans.")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getPlansByVariantId(
            @PathVariable Long variantId,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean activeOnly = principal.getRole() == UserRole.CUSTOMER;
        List<PlanResponse> plans = planService.getPlansByVariantId(variantId, activeOnly);
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    @GetMapping("/api/plans/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get plan by ID", description = "Retrieves plan details. Customers can only view ACTIVE plans.")
    public ResponseEntity<ApiResponse<PlanResponse>> getPlanById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean activeOnly = principal.getRole() == UserRole.CUSTOMER;
        PlanResponse plan = planService.getPlanById(id, activeOnly);
        return ResponseEntity.ok(ApiResponse.success(plan));
    }

    @PutMapping("/api/plans/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Update plan", description = "Updates plan details, pricing, and trial days. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<PlanResponse>> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePlanRequest request) {
        PlanResponse response = planService.updatePlan(id, request);
        return ResponseEntity.ok(ApiResponse.success("Plan updated successfully", response));
    }

    @PatchMapping("/api/plans/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Update plan status", description = "Activates or deactivates a plan. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<PlanResponse>> updatePlanStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePlanStatusRequest request) {
        PlanResponse response = planService.updatePlanStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Plan status updated successfully", response));
    }
}

