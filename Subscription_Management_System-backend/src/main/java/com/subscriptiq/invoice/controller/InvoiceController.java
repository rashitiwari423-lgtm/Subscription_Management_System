package com.subscriptiq.invoice.controller;

import com.subscriptiq.common.response.ApiResponse;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.invoice.dto.CreateInvoiceRequest;
import com.subscriptiq.invoice.dto.InvoiceResponse;
import com.subscriptiq.invoice.dto.UpdateInvoiceStatusRequest;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import com.subscriptiq.invoice.service.InvoiceService;
import com.subscriptiq.security.UserPrincipal;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoice lifecycle, billing calculations, and status management APIs")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/invoices")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Create invoice directly", description = "Creates a direct invoice with calculated subtotal, discounts, taxes and balance. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        InvoiceResponse response = invoiceService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice created successfully", response));
    }

    @PostMapping("/subscriptions/{subscriptionId}/invoice")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Create invoice from subscription", description = "Generates an invoice from an existing subscription with its plan and pricing snapshot. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoiceFromSubscription(@PathVariable Long subscriptionId) {
        InvoiceResponse response = invoiceService.createInvoiceFromSubscription(subscriptionId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice created from subscription successfully", response));
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "List invoices", description = "Paginated list of invoices with optional status filter. CUSTOMER sees only their invoices.")
    public ResponseEntity<ApiResponse<PagedResponse<InvoiceResponse>>> getInvoices(
            @RequestParam(required = false) InvoiceStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {
        PagedResponse<InvoiceResponse> response = invoiceService.getInvoices(status, pageable, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/invoices/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get invoice by ID", description = "Retrieves invoice details. CUSTOMER role can only access their own invoice.")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        InvoiceResponse response = invoiceService.getInvoiceById(id, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/invoices/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Update invoice status", description = "Updates invoice status according to lifecycle transition rules. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateInvoiceStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInvoiceStatusRequest request) {
        InvoiceResponse response = invoiceService.updateInvoiceStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Invoice status updated successfully", response));
    }

    @PostMapping("/invoices/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Cancel invoice", description = "Cancels an unpaid invoice. Invoices with recorded payments cannot be cancelled. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<InvoiceResponse>> cancelInvoice(@PathVariable Long id) {
        InvoiceResponse response = invoiceService.cancelInvoice(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice cancelled successfully", response));
    }

    @GetMapping("/customers/{customerId}/invoices")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get customer invoices", description = "Retrieves all invoices for a specific customer. CUSTOMER can only access their own invoices.")
    public ResponseEntity<ApiResponse<PagedResponse<InvoiceResponse>>> getInvoicesByCustomerId(
            @PathVariable Long customerId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {
        PagedResponse<InvoiceResponse> response = invoiceService.getInvoicesByCustomerId(customerId, pageable, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

