package com.subscriptiq.scheduler.controller;

import com.subscriptiq.common.response.ApiResponse;
import com.subscriptiq.scheduler.dto.BillingRunSummary;
import com.subscriptiq.scheduler.service.RecurringBillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/billing")
@RequiredArgsConstructor
@Tag(name = "Admin Billing", description = "Administrative operations for recurring billing and automated invoicing")
public class AdminBillingController {

    private final RecurringBillingService recurringBillingService;

    @PostMapping("/run")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Trigger recurring billing job", description = "Executes the recurring billing workflow to generate invoices for due active subscriptions and mark overdue invoices. Restricted to ADMIN.")
    public ResponseEntity<ApiResponse<BillingRunSummary>> runRecurringBilling() {
        BillingRunSummary summary = recurringBillingService.runRecurringBilling();
        return ResponseEntity.ok(ApiResponse.success("Recurring billing executed successfully", summary));
    }
}

