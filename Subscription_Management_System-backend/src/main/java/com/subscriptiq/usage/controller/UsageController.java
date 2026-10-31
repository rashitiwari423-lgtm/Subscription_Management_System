package com.subscriptiq.usage.controller;

import com.subscriptiq.common.response.ApiResponse;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.usage.dto.CreateUsageRecordRequest;
import com.subscriptiq.usage.dto.UsageRecordResponse;
import com.subscriptiq.usage.service.UsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
@Tag(name = "Usage Tracking", description = "Record, query, and monitor customer subscription resource usage")
public class UsageController {

    private final UsageService usageService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Record resource usage", description = "Creates a resource usage snapshot for a customer subscription. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<UsageRecordResponse>> recordUsage(
            @Valid @RequestBody CreateUsageRecordRequest request) {
        UsageRecordResponse response = usageService.recordUsage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usage record created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get usage record by ID", description = "Retrieves a usage record. CUSTOMER can only view their own usage data.")
    public ResponseEntity<ApiResponse<UsageRecordResponse>> getUsageRecordById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        UsageRecordResponse response = usageService.getUsageRecordById(id, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "List all usage records", description = "Paginated list of all usage records across customers. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<PagedResponse<UsageRecordResponse>>> getAllUsageRecords(
            @PageableDefault(size = 10, sort = "snapshotDate", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<UsageRecordResponse> response = usageService.getAllUsageRecords(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "List customer usage records", description = "Retrieves usage records for a specific customer. CUSTOMER can only view their own records.")
    public ResponseEntity<ApiResponse<PagedResponse<UsageRecordResponse>>> getCustomerUsageRecords(
            @PathVariable Long customerId,
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10, sort = "snapshotDate", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<UsageRecordResponse> response = usageService.getCustomerUsageRecords(customerId, principal, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/subscription/{subscriptionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "List subscription usage records", description = "Retrieves usage records for a specific subscription. CUSTOMER can only view their own records.")
    public ResponseEntity<ApiResponse<PagedResponse<UsageRecordResponse>>> getSubscriptionUsageRecords(
            @PathVariable Long subscriptionId,
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10, sort = "snapshotDate", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<UsageRecordResponse> response = usageService.getSubscriptionUsageRecords(subscriptionId, principal, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

