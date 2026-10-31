package com.subscriptiq.subscription.controller;

import com.subscriptiq.common.response.ApiResponse;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.subscription.dto.SubscriptionResponse;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import com.subscriptiq.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Subscription lifecycle management, pause, resume, close, and renewal APIs")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/subscriptions")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "List subscriptions", description = "Paginated subscription directory with search and status filter. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<PagedResponse<SubscriptionResponse>>> getSubscriptions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) SubscriptionStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<SubscriptionResponse> response = subscriptionService.getSubscriptions(search, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/subscriptions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get subscription by ID", description = "Retrieves subscription details. CUSTOMER role can only access their own linked subscription.")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscriptionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        SubscriptionResponse response = subscriptionService.getSubscriptionById(id, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/customers/{customerId}/subscriptions")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "List customer subscriptions", description = "Lists subscriptions for a customer. CUSTOMER role can only query their own customer ID.")
    public ResponseEntity<ApiResponse<PagedResponse<SubscriptionResponse>>> getCustomerSubscriptions(
            @PathVariable Long customerId,
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<SubscriptionResponse> response = subscriptionService.getCustomerSubscriptions(customerId, principal, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/subscriptions/{id}/pause")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Pause subscription", description = "Pauses an ACTIVE subscription. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> pauseSubscription(@PathVariable Long id) {
        SubscriptionResponse response = subscriptionService.pauseSubscription(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription paused successfully", response));
    }

    @PatchMapping("/subscriptions/{id}/resume")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Resume subscription", description = "Resumes a PAUSED subscription back to ACTIVE. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> resumeSubscription(@PathVariable Long id) {
        SubscriptionResponse response = subscriptionService.resumeSubscription(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription resumed successfully", response));
    }

    @PatchMapping("/subscriptions/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Close subscription", description = "Terminates an ACTIVE or PAUSED subscription. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> closeSubscription(@PathVariable Long id) {
        SubscriptionResponse response = subscriptionService.closeSubscription(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription closed successfully", response));
    }

    @PatchMapping("/subscriptions/{id}/renew")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Renew subscription", description = "Advances next billing date according to plan billing cycle. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> renewSubscription(@PathVariable Long id) {
        SubscriptionResponse response = subscriptionService.renewSubscription(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription renewed successfully", response));
    }
}

