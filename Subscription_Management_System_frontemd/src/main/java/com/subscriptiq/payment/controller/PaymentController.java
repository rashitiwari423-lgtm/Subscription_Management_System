package com.subscriptiq.payment.controller;

import com.subscriptiq.common.response.ApiResponse;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.payment.dto.CreatePaymentRequest;
import com.subscriptiq.payment.dto.PaymentResponse;
import com.subscriptiq.payment.service.PaymentService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment recording, allocation, and tracking APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/invoices/{invoiceId}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Record invoice payment", description = "Records a payment against an invoice and updates its paid/balance amount and status. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(
            @PathVariable Long invoiceId,
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.recordPayment(invoiceId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment recorded successfully", response));
    }

    @GetMapping("/invoices/{invoiceId}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get invoice payments", description = "Retrieves all payments recorded for a specific invoice. CUSTOMER role can only access their own payments.")
    public ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> getPaymentsByInvoiceId(
            @PathVariable Long invoiceId,
            @PageableDefault(size = 10, sort = "paymentDate", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {
        PagedResponse<PaymentResponse> response = paymentService.getPaymentsByInvoiceId(invoiceId, pageable, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/payments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get payment by ID", description = "Retrieves details of a specific payment. CUSTOMER role can only access their own payment.")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        PaymentResponse response = paymentService.getPaymentById(id, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/customers/{customerId}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get customer payments", description = "Retrieves all payments made by a customer. CUSTOMER role can only access their own payments.")
    public ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> getPaymentsByCustomerId(
            @PathVariable Long customerId,
            @PageableDefault(size = 10, sort = "paymentDate", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {
        PagedResponse<PaymentResponse> response = paymentService.getPaymentsByCustomerId(customerId, pageable, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

