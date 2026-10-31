package com.subscriptiq.quotation.controller;

import com.subscriptiq.common.response.ApiResponse;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.quotation.dto.CreateQuotationRequest;
import com.subscriptiq.quotation.dto.QuotationResponse;
import com.subscriptiq.quotation.dto.UpdateQuotationRequest;
import com.subscriptiq.quotation.dto.UpdateQuotationStatusRequest;
import com.subscriptiq.quotation.enums.QuotationStatus;
import com.subscriptiq.quotation.service.QuotationService;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.subscription.dto.SubscriptionResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Quotations", description = "Quotation management, pricing calculations, status lifecycle, and subscription conversion APIs")
public class QuotationController {

    private final QuotationService quotationService;

    @PostMapping("/quotations")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Create quotation", description = "Creates a new draft quotation with plan pricing snapshot. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<QuotationResponse>> createQuotation(
            @Valid @RequestBody CreateQuotationRequest request) {
        QuotationResponse response = quotationService.createQuotation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quotation created successfully", response));
    }

    @GetMapping("/quotations")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "List quotations", description = "Paginated quotation directory with search and status filter. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<PagedResponse<QuotationResponse>>> getQuotations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) QuotationStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<QuotationResponse> response = quotationService.getQuotations(search, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/quotations/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get quotation by ID", description = "Retrieves quotation details. CUSTOMER role can only access their own linked quotation.")
    public ResponseEntity<ApiResponse<QuotationResponse>> getQuotationById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        QuotationResponse response = quotationService.getQuotationById(id, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/quotations/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Update draft quotation", description = "Modifies a DRAFT quotation's items and terms. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<QuotationResponse>> updateQuotation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuotationRequest request) {
        QuotationResponse response = quotationService.updateQuotation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Quotation updated successfully", response));
    }

    @PatchMapping("/quotations/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Transition quotation status", description = "Updates quotation lifecycle status (DRAFT->SENT, SENT->ACCEPTED/REJECTED, DRAFT->REJECTED). Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<QuotationResponse>> updateQuotationStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuotationStatusRequest request) {
        QuotationResponse response = quotationService.updateQuotationStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Quotation status updated successfully", response));
    }

    @GetMapping("/customers/{customerId}/quotations")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "List customer quotations", description = "Lists quotations for a customer. CUSTOMER role can only query their own customer ID.")
    public ResponseEntity<ApiResponse<PagedResponse<QuotationResponse>>> getCustomerQuotations(
            @PathVariable Long customerId,
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<QuotationResponse> response = quotationService.getCustomerQuotations(customerId, principal, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/quotations/{quotationId}/convert")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Convert accepted quotation to subscription", description = "Transactional conversion of ACCEPTED quotation into an ACTIVE recurring subscription. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> convertQuotation(
            @PathVariable Long quotationId) {
        SubscriptionResponse response = quotationService.convertQuotationToSubscription(quotationId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quotation successfully converted to subscription", response));
    }
}

