package com.subscriptiq.tax.controller;

import com.subscriptiq.common.response.ApiResponse;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.tax.dto.CreateTaxRequest;
import com.subscriptiq.tax.dto.TaxResponse;
import com.subscriptiq.tax.dto.UpdateTaxRequest;
import com.subscriptiq.tax.dto.UpdateTaxStatusRequest;
import com.subscriptiq.tax.enums.TaxStatus;
import com.subscriptiq.tax.service.TaxService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/taxes")
@RequiredArgsConstructor
@Tag(name = "Taxes", description = "Tax management APIs")
public class TaxController {

    private final TaxService taxService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create tax", description = "Creates a new tax rate. Restricted to ADMIN.")
    public ResponseEntity<ApiResponse<TaxResponse>> createTax(@Valid @RequestBody CreateTaxRequest request) {
        TaxResponse response = taxService.createTax(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tax created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "List taxes", description = "Paginated list of taxes with optional status filter. Accessible to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<PagedResponse<TaxResponse>>> getAllTaxes(
            @RequestParam(required = false) TaxStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<TaxResponse> response = taxService.getAllTaxes(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "List active taxes", description = "Returns all active taxes for calculation. Accessible to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<List<TaxResponse>>> getActiveTaxes() {
        List<TaxResponse> response = taxService.getActiveTaxes();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Get tax by ID", description = "Retrieves tax details by ID. Accessible to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<TaxResponse>> getTaxById(@PathVariable Long id) {
        TaxResponse response = taxService.getTaxById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update tax", description = "Updates an existing tax rate. Restricted to ADMIN.")
    public ResponseEntity<ApiResponse<TaxResponse>> updateTax(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaxRequest request) {
        TaxResponse response = taxService.updateTax(id, request);
        return ResponseEntity.ok(ApiResponse.success("Tax updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update tax status", description = "Activates or deactivates a tax. Restricted to ADMIN.")
    public ResponseEntity<ApiResponse<TaxResponse>> updateTaxStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaxStatusRequest request) {
        TaxResponse response = taxService.updateTaxStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Tax status updated successfully", response));
    }
}

