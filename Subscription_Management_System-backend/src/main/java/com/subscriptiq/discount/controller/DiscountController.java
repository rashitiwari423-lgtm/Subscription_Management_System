package com.subscriptiq.discount.controller;

import com.subscriptiq.common.response.ApiResponse;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.discount.dto.CreateDiscountRequest;
import com.subscriptiq.discount.dto.DiscountResponse;
import com.subscriptiq.discount.dto.UpdateDiscountRequest;
import com.subscriptiq.discount.dto.UpdateDiscountStatusRequest;
import com.subscriptiq.discount.enums.DiscountStatus;
import com.subscriptiq.discount.service.DiscountService;
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
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
@Tag(name = "Discounts", description = "Discount management APIs")
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create discount", description = "Creates a new discount rule. Restricted to ADMIN.")
    public ResponseEntity<ApiResponse<DiscountResponse>> createDiscount(@Valid @RequestBody CreateDiscountRequest request) {
        DiscountResponse response = discountService.createDiscount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Discount created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "List discounts", description = "Paginated list of discounts with optional status filter. Accessible to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<PagedResponse<DiscountResponse>>> getAllDiscounts(
            @RequestParam(required = false) DiscountStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<DiscountResponse> response = discountService.getAllDiscounts(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "List active discounts", description = "Returns all active discounts for calculation. Accessible to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<List<DiscountResponse>>> getActiveDiscounts() {
        List<DiscountResponse> response = discountService.getActiveDiscounts();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Get discount by ID", description = "Retrieves discount details by ID. Accessible to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<DiscountResponse>> getDiscountById(@PathVariable Long id) {
        DiscountResponse response = discountService.getDiscountById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update discount", description = "Updates an existing discount rule. Restricted to ADMIN.")
    public ResponseEntity<ApiResponse<DiscountResponse>> updateDiscount(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDiscountRequest request) {
        DiscountResponse response = discountService.updateDiscount(id, request);
        return ResponseEntity.ok(ApiResponse.success("Discount updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update discount status", description = "Activates or deactivates a discount. Restricted to ADMIN.")
    public ResponseEntity<ApiResponse<DiscountResponse>> updateDiscountStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDiscountStatusRequest request) {
        DiscountResponse response = discountService.updateDiscountStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Discount status updated successfully", response));
    }
}

